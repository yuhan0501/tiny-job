package com.tiny_job.admin.thread;

import com.tiny_job.admin.dao.JobInfoHelper;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.utils.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-17 16:19
 **/
@Component
public class JobScheduleHelper {
    private static Logger logger = LoggerFactory.getLogger(JobScheduleHelper.class);
    private Thread scheduleThread;
    private volatile boolean running = false;
    private final int PRE_SIZE = 10;
    private final int PRE_READ_TIME = 5000;

    @Resource
    private JobInfoHelper jobInfoHelper;

    @Autowired
    private JobTriggerPoolHelper triggerPoolHelper;

    public void start() {
        running = true;
        scheduleThread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                long sleepMillis = PRE_READ_TIME - System.currentTimeMillis() % 1000;
                if (sleepMillis > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(sleepMillis);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                long nowTime = System.currentTimeMillis();
                try {
                    // 为了避免每次只计算单个任务的开销，这里批量查询一段时间窗口内的任务。窗口长度为预读时间的两倍，
                    // 这样可以让调度线程有充足的时间在锁定记录后计算下一次触发时间并把任务投递到触发线程池。
                    List<JobInfo> jobInfos = jobInfoHelper.scheduleJobQuery(nowTime + PRE_READ_TIME * 2L, PRE_SIZE);
                    for (JobInfo jobInfo : jobInfos) {
                        try {
                            scheduleSingleJob(jobInfo, nowTime);
                        }
                        catch (Exception e) {
                            logger.error("schedule job error, jobInfo{}", jobInfo, e);
                        }
                    }
                }
                catch (Exception e) {
                    logger.error("scheduler job error", e);
                }
            }

        });
        // 守护线程可以让调度器在 Spring 容器关闭时自动退出，避免阻塞应用关闭流程。
        scheduleThread.setDaemon(true);
        scheduleThread.start();
    }

    private void scheduleSingleJob(JobInfo jobInfo, long nowTime) throws ParseException {
        Long originalNextTimeValue = jobInfo.getTriggerNextTime();
        if (originalNextTimeValue == null) {
            logger.warn("skip job {} because triggerNextTime is null", jobInfo.getId());
            return;
        }
        long originalNextTime = originalNextTimeValue;
        Timestamp originalUpdateTime = jobInfo.getUpdateTime();
        long lockTriggerTime = nowTime + PRE_READ_TIME * 3L;
        if (!jobInfoHelper.lockJobForSchedule(jobInfo, lockTriggerTime)) {
            logger.debug("skip job {} because it is locked by another scheduler", jobInfo.getId());
            return;
        }
        Timestamp lockedUpdateTime = jobInfo.getUpdateTime();
        jobInfo.setTriggerNextTime(originalNextTime);

        boolean committed = false;
        try {
            // 在锁定后的对象快照上计算触发计划，保证时间窗口的一致性。
            // buildSchedulePlan 会根据 cron 表达式计算当前锁定窗口内的所有触发点，
            // 并给出下一次触发时间，用于更新数据库状态。
            SchedulePlan schedulePlan = buildSchedulePlan(jobInfo, nowTime);
            for (TriggerSlot slot : schedulePlan.getTriggerSlots()) {
                // 触发线程池在独立线程中执行，为了避免多线程共享同一个 JobInfo 实例，
                // 这里拷贝一份快照并只携带必要的时间信息。
                JobInfo snapshot = new JobInfo();
                BeanUtils.copyProperties(jobInfo, snapshot);
                snapshot.setTriggerLastTime(slot.getPreviousFireTime());
                snapshot.setTriggerNextTime(slot.getFireTime());
                triggerPoolHelper.triggerJob(snapshot, slot.getFireTime() - nowTime);
            }

            Long nextTriggerTime = schedulePlan.getNextTriggerTime();
            if (nextTriggerTime == null) {
                nextTriggerTime = lockTriggerTime;
                logger.warn("no next trigger time calculated for job {}, fallback to lock window {}", jobInfo.getId(), lockTriggerTime);
            }

            Long lastTriggerTime = schedulePlan.getLastTriggerTime();
            // finalizeSchedule 会在数据库层面验证 update_time 与锁定时一致，
            // 从而保证只有当前线程可以提交下一次触发时间，防止并发调度重复触发。
            committed = jobInfoHelper.finalizeSchedule(jobInfo, lockedUpdateTime, lockTriggerTime, lastTriggerTime, nextTriggerTime);
            if (!committed) {
                logger.warn("failed to finalize schedule for job {}", jobInfo.getId());
            }
        }
        finally {
            if (!committed) {
                // 调度失败时需要把 trigger_next_time 还原，避免后续任务长期处于锁定窗口。
                jobInfoHelper.restoreScheduleLock(jobInfo, lockedUpdateTime, lockTriggerTime, originalNextTime, originalUpdateTime);
            }
        }
    }

    private SchedulePlan buildSchedulePlan(JobInfo jobInfo, long nowTime) throws ParseException {
        CronExpression cronExpression = new CronExpression(jobInfo.getJobCron());
        long windowLimit = nowTime + PRE_READ_TIME;
        Long nextTriggerValue = jobInfo.getTriggerNextTime();
        if (nextTriggerValue == null) {
            return new SchedulePlan(Collections.emptyList(), jobInfo.getTriggerLastTime(), null);
        }
        long nextTriggerTime = nextTriggerValue;
        List<TriggerSlot> slots = new ArrayList<>();
        Long previousFireTime = jobInfo.getTriggerLastTime();
        while (nextTriggerTime > 0 && nextTriggerTime <= windowLimit) {
            slots.add(new TriggerSlot(nextTriggerTime, previousFireTime));
            previousFireTime = nextTriggerTime;
            Date nextValid = cronExpression.getNextValidTimeAfter(new Date(nextTriggerTime));
            if (nextValid == null) {
                return new SchedulePlan(slots, previousFireTime, null);
            }
            nextTriggerTime = nextValid.getTime();
        }
        return new SchedulePlan(slots, previousFireTime, nextTriggerTime);
    }

    @PreDestroy
    public void destroy() {
        running = false;
        if (scheduleThread != null) {
            scheduleThread.interrupt();
        }
    }

    private static final class TriggerSlot {
        private final long fireTime;
        private final Long previousFireTime;

        private TriggerSlot(long fireTime, Long previousFireTime) {
            this.fireTime = fireTime;
            this.previousFireTime = previousFireTime;
        }

        public long getFireTime() {
            return fireTime;
        }

        public Long getPreviousFireTime() {
            return previousFireTime;
        }
    }

    private static final class SchedulePlan {
        private final List<TriggerSlot> triggerSlots;
        private final Long lastTriggerTime;
        private final Long nextTriggerTime;

        private SchedulePlan(List<TriggerSlot> triggerSlots, Long lastTriggerTime, Long nextTriggerTime) {
            this.triggerSlots = Collections.unmodifiableList(triggerSlots);
            this.lastTriggerTime = lastTriggerTime;
            this.nextTriggerTime = nextTriggerTime;
        }

        public List<TriggerSlot> getTriggerSlots() {
            return triggerSlots;
        }

        public Long getLastTriggerTime() {
            return lastTriggerTime;
        }

        public Long getNextTriggerTime() {
            return nextTriggerTime;
        }
    }
}

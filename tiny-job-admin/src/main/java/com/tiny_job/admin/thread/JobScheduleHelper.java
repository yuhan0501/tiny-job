package com.tiny_job.admin.thread;

import com.tiny_job.admin.dao.JobInfoHelper;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.utils.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private final int PRE_SIZE = 10;
    private final int PRE_READ_TIME = 5000;

    @Resource
    private JobInfoHelper jobInfoHelper;

    @Autowired
    private JobTriggerPoolHelper triggerPoolHelper;

    public void start() {
        scheduleThread = new Thread(() -> {
            while (true) {
                //休眠一段时间
                try {
                    TimeUnit.MILLISECONDS.sleep(PRE_READ_TIME - System.currentTimeMillis() % 1000);
                }
                catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                Long nowTime = System.currentTimeMillis();
                try {
                    List<JobInfo> jobInfos = jobInfoHelper.scheduleJobQuery(nowTime + PRE_READ_TIME * 2, PRE_SIZE);
                    jobInfos.forEach(jobInfo -> {
                        try {
                            int resultCount = jobInfoHelper.updateByOptimisticLock(jobInfo);
                            //影响的记录为0说明已经被其他线程处理，跳过此次job
                            if (resultCount == 0) {
                                logger.debug("NO task to scheduler");
                                return;
                            }
                            logger.debug("Try to trigger job", jobInfo);

                            //已经过期的任务，立即调度一次
                            if (jobInfo.getTriggerNextTime() < nowTime) {
                                triggerPoolHelper.triggerJob(jobInfo, jobInfo.getTriggerNextTime() - nowTime);
                                refreshNextValidTime(jobInfo, new Date(nowTime));
                            }
                            //判断是否是即将要调度
                            checkHighFrequency(jobInfo, nowTime);
                            logTime("final {}", jobInfo.getTriggerNextTime());
                            jobInfoHelper.updateWithoutOptimisticLock(jobInfo);
                        }
                        catch (Exception e) {
                            logger.error("schedule job error,jobInfo{}", jobInfo, e);
                        }
                    });
                }
                catch (Exception e) {
                    logger.error("scheduler job error", e);
                }
            }

        });
        scheduleThread.setDaemon(true);
        scheduleThread.start();
    }

    /**
     * 如果是超频繁任务，递归生成最近一个周期内的所有触发任务
     *
     * @param jobInfo
     */
    private void checkHighFrequency(JobInfo jobInfo, Long nowTime) throws ParseException {
        if (jobInfo.getTriggerNextTime() < (nowTime + PRE_READ_TIME)) {
            //将任务放入待执行队列
            triggerPoolHelper.triggerJob(jobInfo, jobInfo.getTriggerNextTime() - nowTime);
            //更新下次调度job的时间
            refreshNextValidTime(jobInfo, new Date(jobInfo.getTriggerNextTime()));
            //判断是否是超高频繁任务，即调度周期小于5s一次
            checkHighFrequency(jobInfo, nowTime);
        }
    }


    private void refreshNextValidTime(JobInfo jobInfo, Date fromTime) throws ParseException {
        Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(fromTime);
        if (nextValidTime != null) {
            jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
            jobInfo.setTriggerNextTime(nextValidTime.getTime());
            logTime("nnxt", nextValidTime.getTime());
        }
        else {
            logger.warn("nextValidTime is null");
        }
    }

    private void logTime(String msg, Long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
        logger.info("{}:{}", msg, simpleDateFormat.format(time));
    }

}

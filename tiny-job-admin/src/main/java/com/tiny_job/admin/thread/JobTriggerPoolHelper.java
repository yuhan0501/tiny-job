package com.tiny_job.admin.thread;

import com.tiny_job.admin.config.TinyJobConfig;
import com.tiny_job.admin.dao.JobInfoHelper;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.executor.TinyJobExecutorBaseAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 20:10
 **/
@Component
public class JobTriggerPoolHelper {

    private static Logger logger = LoggerFactory.getLogger(JobTriggerPoolHelper.class);
    @Autowired
    private TinyJobConfig tinyJobConfig;

    @Resource
    private JobInfoHelper jobInfoHelper;

    // 使用 ConcurrentHashMap 保证在触发线程异步读取执行器时的可见性与线程安全。
    private final Map<String, TinyJobExecutorBaseAdapter> tinyJobExecutor = new ConcurrentHashMap<>();

    @Autowired(required = false)
    public void setTinyJobExecutor(Map<String, TinyJobExecutorBaseAdapter> executors) {
        tinyJobExecutor.clear();
        if (executors != null) {
            tinyJobExecutor.putAll(executors);
        }
    }

    private ScheduledThreadPoolExecutor triggerPool;

    public void init() {
        int poolSize = tinyJobConfig.getTriggerPollSize() != null && tinyJobConfig.getTriggerPollSize() > 0
                ? tinyJobConfig.getTriggerPollSize()
                : Runtime.getRuntime().availableProcessors();
        // ScheduledThreadPoolExecutor 提供了延迟调度能力，可确保每个任务按照计算出的时间被触发。
        // 自定义线程工厂开启守护线程，避免应用关闭时阻塞退出。
        triggerPool = new ScheduledThreadPoolExecutor(poolSize, runnable -> {
            Thread thread = new Thread(runnable, "JobTriggerPoolHelper");
            thread.setDaemon(true);
            return thread;
        });
        triggerPool.setRemoveOnCancelPolicy(true);
        triggerPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        triggerPool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
//                new ThreadPoolExecutor(
//                10,
//                tinyJobConfig.getTriggerPollSize(),
//                60L,
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<Runnable>(1000),
//                runnable -> new Thread(runnable, " JobTriggerPoolHelper")
//        );
    }

    public void triggerJob(JobInfo jobInfo, long delay) {
        logger.debug("trigger job:{}", delay);
        JobInfo copyOf = new JobInfo();
        BeanUtils.copyProperties(jobInfo, copyOf);
        JobTriggerThread triggerThread = new JobTriggerThread(copyOf, tinyJobExecutor.get(jobInfo.getJobType()));
        //小于0说明是延期的任务，立即执行
        if (delay <= 0) {
            triggerPool.execute(triggerThread);

        }
        //大于0说明还未到调度时间,延迟调度
        else {
            triggerPool.schedule(triggerThread, delay, TimeUnit.MILLISECONDS);
        }

    }

    @PreDestroy
    public void shutdown() {
        if (triggerPool == null) {
            return;
        }
        // 优雅关闭线程池，尽可能等待已提交的任务完成，避免调度线程提前退出导致任务丢失。
        triggerPool.shutdown();
        try {
            if (!triggerPool.awaitTermination(10, TimeUnit.SECONDS)) {
                triggerPool.shutdownNow();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            triggerPool.shutdownNow();
        }
        logger.info("shut down JobTriggerPoolHelper success");
    }
}

package com.tiny_job.admin.thread;

import com.tiny_job.admin.config.TinyJobConfig;
import com.tiny_job.admin.dao.JobInfoHelper;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.executor.TinyJobExecutorBaseAdapter;
import com.tiny_job.admin.control.ExecutionControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    @Autowired
    private ExecutionControlService executionControlService;

    @Autowired
    private Map<String, TinyJobExecutorBaseAdapter> tinyJobExecutor = new ConcurrentHashMap<>();

    private ScheduledThreadPoolExecutor triggerPool;

    public void init() {
        triggerPool = new ScheduledThreadPoolExecutor(tinyJobConfig.getTriggerPollSize(), runnable -> new Thread(runnable, " JobTriggerPoolHelper"));
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
        if (executionControlService.isPaused()) {
            logger.debug("global pause active, skip trigger for job {}", jobInfo.getId());
            return;
        }
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

    public void shutdown() {
        triggerPool.shutdown();
        while (true) {
            if (triggerPool.isTerminated()) {
                logger.info("shut down JobTriggerPoolHelper success");
                break;
            }
        }
    }
}

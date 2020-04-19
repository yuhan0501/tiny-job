package com.tiny_job.admin.core.thread;

import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.executor.TinyJobExecutorBaseAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-18 14:52
 **/
public class JobTriggerThread implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(JobTriggerThread.class);

    public JobTriggerThread(JobInfo jobInfo, TinyJobExecutorBaseAdapter adapter) {
        this.adapter = adapter;
        this.jobInfo = jobInfo;
    }

    private JobInfo jobInfo;
    private TinyJobExecutorBaseAdapter adapter;

    @Override
    public void run() {
        adapter.processJob(jobInfo);
        logTime("success", jobInfo.getTriggerNextTime());
    }

    private void logTime(String msg, Long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
        logger.info("{}:{}", msg, simpleDateFormat.format(time));
    }
}

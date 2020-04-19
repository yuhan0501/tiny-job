package com.tiny_job.admin.executor;

import com.tiny_job.admin.dao.entity.JobInfo;

/**
 * @description: 此类将根据配置好的服务发起调用
 * @author: yuhan
 * @create: 2020-04-16 20:10
 **/
public interface TinyJobExecutorBaseAdapter {


    void processJob(JobInfo jobInfo);
}

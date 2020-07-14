package com.tiny_job.admin.config;

import com.tiny_job.admin.thread.JobScheduleHelper;
import com.tiny_job.admin.thread.JobTriggerPoolHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-05-20 17:13
 **/
@Configuration
public class TinyJobInitializing implements InitializingBean {

    @Autowired
    private JobTriggerPoolHelper jobTriggerPoolHelper;
    @Autowired
    private JobScheduleHelper jobScheduleHelper;

    @Override
    public void afterPropertiesSet() throws Exception {
        jobTriggerPoolHelper.init();
        jobScheduleHelper.start();
    }
}

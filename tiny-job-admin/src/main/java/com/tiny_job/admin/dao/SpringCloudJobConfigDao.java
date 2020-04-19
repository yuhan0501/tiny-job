package com.tiny_job.admin.dao;

import com.tiny_job.admin.dao.entity.JobConfig;
import com.tiny_job.admin.dao.entity.JobSpringCloudConfig;
import com.tiny_job.admin.dao.mapper.JobSpringCloudConfigMapper;
import com.tiny_job.admin.exception.TinyJobExceptionAssert;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 22:05
 **/
@Repository("spring_cloud_dao")
public class SpringCloudJobConfigDao implements BaseJobConfigDao {
    @Resource
    private JobSpringCloudConfigMapper jobSpringCloudConfigMapper;


    @Override
    public JobConfig findByJobId(Long jobId) {
        JobSpringCloudConfig jobConfig = jobSpringCloudConfigMapper.selectByPrimaryKey(jobId);
        TinyJobExceptionAssert.notNull(jobConfig, "There is no record in job_spring_cloud_config with jobId:" + jobId);
        return jobConfig;
    }
}

package com.tiny_job.admin.dao;

import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.dao.mapper.JobConfigMapper;
import com.tiny_job.admin.dao.mapper.JobInfoMapper;
import com.tiny_job.admin.exception.TinyJobExceptionAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 21:47
 **/
@Repository
public class JobInfoHelper {
    private static Logger logger = LoggerFactory.getLogger(JobInfoHelper.class);
    @Resource
    private JobInfoMapper jobInfoMapper;

    @Resource
    private JobConfigMapper jobConfigMapper;

    /**
     * 基于乐观锁进行变更，乐观锁字段update_time
     *
     * @param jobInfo
     * @return 影响记录
     */
    public int updateByOptimisticLock(JobInfo jobInfo) {
        return jobInfoMapper.updateByPrimaryKey(jobInfo);
    }

    public int updateWithoutOptimisticLock(JobInfo jobInfo) {
        JobInfo dbInfo = jobInfoMapper.selectByPrimaryKey(jobInfo.getId());
        jobInfo.setUpdateTime(dbInfo.getUpdateTime());
        return jobInfoMapper.updateByPrimaryKey(jobInfo);
    }

    /**
     * 获取未调度的任务
     *
     * @param scheduleTime 调度时间，trigger_next_time小于这个时间的将被捞出
     * @param preSize
     * @return 任务合集
     */
    public List<JobInfo> scheduleJobQuery(Long scheduleTime, int preSize) {

        return jobInfoMapper.scheduleJobQuery(scheduleTime, preSize);
    }

    /**
     * 通过jobId构造出完整的JobInfo
     *
     * @param jobId
     * @return
     */
    public JobInfo findJobById(Long jobId) {
        //查询job_info表，捞出job基础信息
        JobInfo jobInfo = jobInfoMapper.selectByPrimaryKey(jobId);
        TinyJobExceptionAssert.notNull(jobInfo, "no such job id in db:" + jobId);
        //根据job_type注入不同的job_config
        jobInfo.setJobConfig(jobConfigMapper.selectByPrimaryKey(jobInfo.getConfigId()));
        logger.debug("JobInfoHelper.findJobById:", jobInfo);
        return jobInfo;
    }
     

}

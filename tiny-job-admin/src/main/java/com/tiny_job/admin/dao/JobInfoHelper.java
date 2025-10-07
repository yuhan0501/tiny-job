package com.tiny_job.admin.dao;

import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.dao.mapper.JobConfigMapper;
import com.tiny_job.admin.dao.mapper.JobInfoMapper;
import com.tiny_job.admin.exception.TinyJobExceptionAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Condition;

import javax.annotation.Resource;
import java.sql.Timestamp;
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
        Condition condition = new Condition(JobInfo.class);
        condition.createCriteria()
                .andEqualTo("id", jobInfo.getId())
                .andEqualTo("updateTime", jobInfo.getUpdateTime());

        Timestamp newUpdateTime = new Timestamp(System.currentTimeMillis());
        JobInfo updateEntity = new JobInfo();
        updateEntity.setId(jobInfo.getId());
        updateEntity.setUpdateTime(newUpdateTime);
        int updateCount = jobInfoMapper.updateByConditionSelective(updateEntity, condition);
        if (updateCount > 0) {
            jobInfo.setUpdateTime(newUpdateTime);
        }
        return updateCount;
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

    public boolean lockJobForSchedule(JobInfo jobInfo, long lockTriggerTime) {
        // 通过同时匹配 update_time 与 trigger_next_time 来实现软锁，
        // 保证只有一个调度线程可以进入锁定窗口并修改下一次触发时间。
        Condition condition = new Condition(JobInfo.class);
        condition.createCriteria()
                .andEqualTo("id", jobInfo.getId())
                .andEqualTo("updateTime", jobInfo.getUpdateTime())
                .andEqualTo("triggerNextTime", jobInfo.getTriggerNextTime());

        Timestamp lockTimestamp = new Timestamp(System.currentTimeMillis());
        JobInfo updateEntity = new JobInfo();
        updateEntity.setId(jobInfo.getId());
        updateEntity.setUpdateTime(lockTimestamp);
        updateEntity.setTriggerNextTime(lockTriggerTime);
        int updateCount = jobInfoMapper.updateByConditionSelective(updateEntity, condition);
        if (updateCount > 0) {
            jobInfo.setUpdateTime(lockTimestamp);
            jobInfo.setTriggerNextTime(lockTriggerTime);
            return true;
        }
        return false;
    }

    public boolean finalizeSchedule(JobInfo jobInfo, Timestamp lockedUpdateTime, long lockedTriggerTime,
                                    Long newTriggerLastTime, Long newTriggerNextTime) {
        // finalizeSchedule 与 lockJobForSchedule 使用完全相同的条件，确保只有当前锁持有者
        //（update_time 与 trigger_next_time 均未被其他线程修改）才能提交新的时间窗口。
        Condition condition = new Condition(JobInfo.class);
        condition.createCriteria()
                .andEqualTo("id", jobInfo.getId())
                .andEqualTo("updateTime", lockedUpdateTime)
                .andEqualTo("triggerNextTime", lockedTriggerTime);

        Timestamp newUpdateTime = new Timestamp(System.currentTimeMillis());
        JobInfo updateEntity = new JobInfo();
        updateEntity.setId(jobInfo.getId());
        updateEntity.setTriggerLastTime(newTriggerLastTime);
        updateEntity.setTriggerNextTime(newTriggerNextTime);
        updateEntity.setUpdateTime(newUpdateTime);
        int updateCount = jobInfoMapper.updateByConditionSelective(updateEntity, condition);
        if (updateCount > 0) {
            jobInfo.setTriggerLastTime(newTriggerLastTime);
            jobInfo.setTriggerNextTime(newTriggerNextTime);
            jobInfo.setUpdateTime(newUpdateTime);
            return true;
        }
        return false;
    }

    public void restoreScheduleLock(JobInfo jobInfo, Timestamp lockedUpdateTime, long lockedTriggerTime,
                                    Long originalTriggerNextTime, Timestamp originalUpdateTime) {
        // 若 finalize 失败或出现异常，需要利用锁定时的快照还原 trigger_next_time，
        // 防止记录一直停留在锁定窗口无法再次被调度线程扫描。
        Condition condition = new Condition(JobInfo.class);
        condition.createCriteria()
                .andEqualTo("id", jobInfo.getId())
                .andEqualTo("updateTime", lockedUpdateTime)
                .andEqualTo("triggerNextTime", lockedTriggerTime);

        JobInfo updateEntity = new JobInfo();
        updateEntity.setId(jobInfo.getId());
        updateEntity.setTriggerNextTime(originalTriggerNextTime);
        Timestamp fallbackUpdateTime = originalUpdateTime != null ? originalUpdateTime : new Timestamp(System.currentTimeMillis());
        updateEntity.setUpdateTime(fallbackUpdateTime);
        if (jobInfoMapper.updateByConditionSelective(updateEntity, condition) > 0) {
            jobInfo.setTriggerNextTime(originalTriggerNextTime);
            jobInfo.setUpdateTime(fallbackUpdateTime);
        }
    }

    public List<JobInfo> jobInfoList(Integer jobStatus,String jobDesc,String jobType){
        Condition condition = new Condition(JobInfo.class);
        Condition.Criteria criteria = condition.createCriteria();
        if (jobStatus != null) {
            criteria.andEqualTo("jobStatus", jobStatus);
        }
        if (StringUtils.hasText(jobDesc)) {
            criteria.andLike("jobDesc", "%" + jobDesc.trim() + "%");
        }
        if (StringUtils.hasText(jobType)) {
            criteria.andEqualTo("jobType", jobType.trim());
        }
        condition.orderBy("id").desc();
        return jobInfoMapper.selectByCondition(condition);
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

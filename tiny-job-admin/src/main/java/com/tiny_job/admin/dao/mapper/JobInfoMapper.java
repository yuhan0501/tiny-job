package com.tiny_job.admin.dao.mapper;

import com.tiny_job.admin.dao.entity.JobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.special.InsertUseGeneratedKeysMapper;

import java.util.List;

@Mapper
public interface JobInfoMapper extends tk.mybatis.mapper.common.Mapper<JobInfo>,
        InsertUseGeneratedKeysMapper<JobInfo> {

    @Select("select id, job_cron, job_desc, author, job_type, config_id, execute_block_strategy, execute_timeout, execute_fail_retry_count, child_job_id, job_status, trigger_last_time, trigger_next_time, job_version,update_time, create_time " +
            "from job_info " +
            "where job_status = 1 " +
            "and trigger_next_time <= #{triggerTime} " +
            "limit #{size}")
    List<JobInfo> scheduleJobQuery(@Param("triggerTime") Long triggerTime, @Param("size") int size);


}

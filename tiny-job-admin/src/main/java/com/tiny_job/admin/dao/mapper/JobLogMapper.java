package com.tiny_job.admin.dao.mapper;

import com.tiny_job.admin.dao.entity.JobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.special.InsertUseGeneratedKeysMapper;

import java.sql.Timestamp;
import java.util.List;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-23 19:46
 **/
@Mapper
public interface JobLogMapper extends tk.mybatis.mapper.common.Mapper<JobLog>,
        InsertUseGeneratedKeysMapper<JobLog> {

    @Select("select * from job_log where trigger_time >= #{since}")
    List<JobLog> selectSince(@Param("since") Timestamp since);
}

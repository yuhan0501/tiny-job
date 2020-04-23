package com.tiny_job.admin.dao.mapper;

import com.tiny_job.admin.dao.entity.JobLog;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.special.InsertUseGeneratedKeysMapper;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-23 19:46
 **/
@Mapper
public interface JobLogMapper extends tk.mybatis.mapper.common.Mapper<JobLog>,
        InsertUseGeneratedKeysMapper<JobLog> {
}

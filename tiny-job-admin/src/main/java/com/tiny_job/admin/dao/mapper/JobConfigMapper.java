package com.tiny_job.admin.dao.mapper;

import com.tiny_job.admin.dao.entity.JobConfig;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.special.InsertUseGeneratedKeysMapper;

@Mapper
public interface JobConfigMapper extends tk.mybatis.mapper.common.Mapper<JobConfig>,
        InsertUseGeneratedKeysMapper<JobConfig> {
}

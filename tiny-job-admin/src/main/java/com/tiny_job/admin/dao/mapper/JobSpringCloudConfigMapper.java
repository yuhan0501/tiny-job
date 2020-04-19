package com.tiny_job.admin.dao.mapper;

import com.tiny_job.admin.dao.entity.JobSpringCloudConfig;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.special.InsertUseGeneratedKeysMapper;

@Mapper
public interface JobSpringCloudConfigMapper extends tk.mybatis.mapper.common.Mapper<JobSpringCloudConfig>,
        InsertUseGeneratedKeysMapper<JobSpringCloudConfig> {
}

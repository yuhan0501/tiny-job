package com.tiny_job.admin.dao;

import com.tiny_job.admin.dao.entity.JobConfig;

public interface BaseJobConfigDao {
    JobConfig findByJobId(Long jobId);
}

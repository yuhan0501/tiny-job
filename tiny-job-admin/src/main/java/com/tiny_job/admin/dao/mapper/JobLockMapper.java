package com.tiny_job.admin.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface JobLockMapper {

    @Update("UPDATE job_lock SET owner = #{owner}, expires_at = #{expiresAt} " +
            "WHERE lock_name = #{lockName} AND (owner IS NULL OR owner = #{owner} OR expires_at < #{now})")
    int acquire(@Param("lockName") String lockName,
                @Param("owner") String owner,
                @Param("expiresAt") long expiresAt,
                @Param("now") long now);

    @Update("UPDATE job_lock SET owner = NULL, expires_at = 0 " +
            "WHERE lock_name = #{lockName} AND owner = #{owner}")
    int release(@Param("lockName") String lockName,
                @Param("owner") String owner);
}

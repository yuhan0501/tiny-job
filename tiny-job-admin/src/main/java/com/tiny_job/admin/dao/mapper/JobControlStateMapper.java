package com.tiny_job.admin.dao.mapper;

import com.tiny_job.admin.dao.entity.JobControlState;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface JobControlStateMapper extends tk.mybatis.mapper.common.Mapper<JobControlState> {

    @Update("UPDATE job_control_state " +
            "SET paused = #{paused}, paused_at = #{pausedAt} " +
            "WHERE id = #{id} AND paused = #{expectedPaused}")
    int updatePauseState(@Param("id") int id,
                         @Param("paused") boolean paused,
                         @Param("pausedAt") long pausedAt,
                         @Param("expectedPaused") boolean expectedPaused);
}

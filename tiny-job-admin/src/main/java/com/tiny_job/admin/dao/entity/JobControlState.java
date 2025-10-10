package com.tiny_job.admin.dao.entity;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "job_control_state")
public class JobControlState {

    @Id
    private Integer id;
    private Boolean paused;
    private Long pausedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean isPaused() {
        return paused != null && paused;
    }

    public Boolean getPaused() {
        return paused;
    }

    public void setPaused(Boolean paused) {
        this.paused = paused;
    }

    public Long getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(Long pausedAt) {
        this.pausedAt = pausedAt;
    }
}

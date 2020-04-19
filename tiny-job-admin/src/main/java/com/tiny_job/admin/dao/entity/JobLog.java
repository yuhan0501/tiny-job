package com.tiny_job.admin.dao.entity;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 11:29
 **/

public class JobLog {

    private Long id;
    private Long jobId;
    private String executeService;
    private String executePath;
    private String executeParam;
    private Long executeTimeout;
    private Long executorFailRetryCount;
    private java.util.Date triggerTime;
    private java.util.Date handleTime;
    private Long handleCode;
    private String handleMsg;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }


    public String getExecuteService() {
        return executeService;
    }

    public void setExecuteService(String executeService) {
        this.executeService = executeService;
    }


    public String getExecutePath() {
        return executePath;
    }

    public void setExecutePath(String executePath) {
        this.executePath = executePath;
    }


    public String getExecuteParam() {
        return executeParam;
    }

    public void setExecuteParam(String executeParam) {
        this.executeParam = executeParam;
    }


    public Long getExecuteTimeout() {
        return executeTimeout;
    }

    public void setExecuteTimeout(Long executeTimeout) {
        this.executeTimeout = executeTimeout;
    }


    public Long getExecutorFailRetryCount() {
        return executorFailRetryCount;
    }

    public void setExecutorFailRetryCount(Long executorFailRetryCount) {
        this.executorFailRetryCount = executorFailRetryCount;
    }


    public java.util.Date getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(java.util.Date triggerTime) {
        this.triggerTime = triggerTime;
    }


    public java.util.Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(java.util.Date handleTime) {
        this.handleTime = handleTime;
    }


    public Long getHandleCode() {
        return handleCode;
    }

    public void setHandleCode(Long handleCode) {
        this.handleCode = handleCode;
    }


    public String getHandleMsg() {
        return handleMsg;
    }

    public void setHandleMsg(String handleMsg) {
        this.handleMsg = handleMsg;
    }

    @Override
    public String toString() {
        return "JobLog{" +
                "id=" + id +
                ", jobId=" + jobId +
                ", executeService='" + executeService + '\'' +
                ", executePath='" + executePath + '\'' +
                ", executeParam='" + executeParam + '\'' +
                ", executeTimeout=" + executeTimeout +
                ", executorFailRetryCount=" + executorFailRetryCount +
                ", triggerTime=" + triggerTime +
                ", handleTime=" + handleTime +
                ", handleCode=" + handleCode +
                ", handleMsg='" + handleMsg + '\'' +
                '}';
    }
}

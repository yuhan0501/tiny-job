package com.tiny_job.admin.dao.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 11:29
 **/
@Entity
@Table(name = "job_log")
public class JobLog {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;
    private Long jobId;
    private String executeService;
    private String executeMethod;
    private String executeParam;
    private String serviceType;
    private Long executeTimeout;
    private Long executeFailRetryCount;
    private Timestamp triggerTime;
    private Timestamp handleTime;
    private String handleCode;
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

    public String getExecuteMethod() {
        return executeMethod;
    }

    public void setExecuteMethod(String executeMethod) {
        this.executeMethod = executeMethod;
    }

    public String getExecuteParam() {
        return executeParam;
    }

    public void setExecuteParam(String executeParam) {
        this.executeParam = executeParam;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Long getExecuteTimeout() {
        return executeTimeout;
    }

    public void setExecuteTimeout(Long executeTimeout) {
        this.executeTimeout = executeTimeout;
    }

    public Long getExecuteFailRetryCount() {
        return executeFailRetryCount;
    }

    public void setExecuteFailRetryCount(Long executeFailRetryCount) {
        this.executeFailRetryCount = executeFailRetryCount;
    }

    public Date getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Timestamp triggerTime) {
        this.triggerTime = triggerTime;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Timestamp handleTime) {
        this.handleTime = handleTime;
    }

    public String getHandleCode() {
        return handleCode;
    }

    public void setHandleCode(String handleCode) {
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
                ", executeMethod='" + executeMethod + '\'' +
                ", executeParam='" + executeParam + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", executeTimeout=" + executeTimeout +
                ", executorFailRetryCount=" + executeFailRetryCount +
                ", triggerTime=" + triggerTime +
                ", handleTime=" + handleTime +
                ", handleCode=" + handleCode +
                ", handleMsg='" + handleMsg + '\'' +
                '}';
    }
}

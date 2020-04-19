package com.tiny_job.admin.dao.entity;

import tk.mybatis.mapper.annotation.Version;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @description: 任务基础信息
 * @author: yuhan
 * @create: 2020-04-16 11:18
 **/
@Entity
@Table(name = "job_info")
public class JobInfo {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;
    private String jobCron;
    private String jobDesc;
    private String author;
    private String jobType;
    private Long configId;
    private String executeBlockStrategy;
    private Long executeTimeout;
    private Long executeFailRetryCount;
    private String childJobId;
    private Integer jobStatus;
    private Long triggerLastTime;
    private Long triggerNextTime;
    private Long jobVersion;
    @Version
    private Timestamp updateTime;
    private Timestamp createTime;
    private JobConfig jobConfig;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobCron() {
        return jobCron;
    }

    public void setJobCron(String jobCron) {
        this.jobCron = jobCron;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getExecuteBlockStrategy() {
        return executeBlockStrategy;
    }

    public void setExecuteBlockStrategy(String executeBlockStrategy) {
        this.executeBlockStrategy = executeBlockStrategy;
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

    public String getChildJobId() {
        return childJobId;
    }

    public void setChildJobId(String childJobId) {
        this.childJobId = childJobId;
    }

    public Integer getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(Integer jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Long getTriggerLastTime() {
        return triggerLastTime;
    }

    public void setTriggerLastTime(Long triggerLastTime) {
        this.triggerLastTime = triggerLastTime;
    }

    public Long getTriggerNextTime() {
        return triggerNextTime;
    }

    public void setTriggerNextTime(Long triggerNextTime) {
        this.triggerNextTime = triggerNextTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public JobConfig getJobConfig() {
        return jobConfig;
    }

    public void setJobConfig(JobConfig jobConfig) {
        this.jobConfig = jobConfig;
    }

    public Long getJobVersion() {
        return jobVersion;
    }

    public void setJobVersion(Long jobVersion) {
        this.jobVersion = jobVersion;
    }

    @Override
    public String toString() {
        return "JobInfo{" +
                "id=" + id +
                ", jobCron='" + jobCron + '\'' +
                ", jobDesc='" + jobDesc + '\'' +
                ", author='" + author + '\'' +
                ", jobType='" + jobType + '\'' +
                ", configId=" + configId +
                ", executeBlockStrategy='" + executeBlockStrategy + '\'' +
                ", executeTimeout=" + executeTimeout +
                ", executeFailRetryCount=" + executeFailRetryCount +
                ", childJobId='" + childJobId + '\'' +
                ", jobStatus=" + jobStatus +
                ", triggerLastTime=" + triggerLastTime +
                ", triggerNextTime=" + triggerNextTime +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}

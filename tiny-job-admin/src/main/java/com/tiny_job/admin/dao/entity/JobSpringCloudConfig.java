package com.tiny_job.admin.dao.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 13:12
 **/
@Entity
@Table(name = "job_spring_cloud_config")
public class JobSpringCloudConfig extends JobConfig {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;
    private String executeService;
    private String executePath;
    private String executeParam;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

}

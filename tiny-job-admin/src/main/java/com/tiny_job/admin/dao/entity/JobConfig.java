package com.tiny_job.admin.dao.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 11:31
 **/
@Entity
@Table(name = "job_config")
public class JobConfig {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;
    private String executeService;
    private String executeMethod;
    private String executeParam;
    private String serviceType;

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

    @Override
    public String toString() {
        return "JobConfig{" +
                "id=" + id +
                ", executeService='" + executeService + '\'' +
                ", executePath='" + executeMethod + '\'' +
                ", executeParam='" + executeParam + '\'' +
                ", service_type='" + serviceType + '\'' +
                '}';
    }
}

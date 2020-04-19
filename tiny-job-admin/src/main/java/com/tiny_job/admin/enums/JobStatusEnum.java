package com.tiny_job.admin.enums;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-18 10:58
 **/
public enum JobStatusEnum {

    STOP(0),
    NORMAL(1),
    SCHEDULING(2);

    private Integer status;

    JobStatusEnum(Integer status) {
        this.status = status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}

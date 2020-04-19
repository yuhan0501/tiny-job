package com.tiny_job.admin.dao.entity;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 11:18
 **/
public class JobLock {
    private String lockName;

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    @Override
    public String toString() {
        return "JobLock{" +
                "lockName='" + lockName + '\'' +
                '}';
    }
}

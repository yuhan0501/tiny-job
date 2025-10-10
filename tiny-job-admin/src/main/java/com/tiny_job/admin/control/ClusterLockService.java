package com.tiny_job.admin.control;

import com.tiny_job.admin.dao.mapper.JobLockMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * 简单的数据库分布式锁，保障多实例下的调度互斥。
 */
@Component
public class ClusterLockService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterLockService.class);
    private final JobLockMapper jobLockMapper;
    private final String ownerId;

    public ClusterLockService(JobLockMapper jobLockMapper) {
        this.jobLockMapper = jobLockMapper;
        this.ownerId = generateOwnerId();
    }

    public boolean tryLock(String lockName, long leaseMillis) {
        long now = System.currentTimeMillis();
        long expiresAt = now + leaseMillis;
        int updated = jobLockMapper.acquire(lockName, ownerId, expiresAt, now);
        boolean acquired = updated > 0;
        if (acquired) {
            logger.debug("acquired lock {} with owner {}", lockName, ownerId);
        }
        return acquired;
    }

    public void unlock(String lockName) {
        int updated = jobLockMapper.release(lockName, ownerId);
        if (updated > 0) {
            logger.debug("released lock {} with owner {}", lockName, ownerId);
        }
    }

    private String generateOwnerId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID();
        } catch (UnknownHostException e) {
            return UUID.randomUUID().toString();
        }
    }
}

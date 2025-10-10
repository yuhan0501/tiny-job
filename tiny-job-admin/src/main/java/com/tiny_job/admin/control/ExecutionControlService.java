package com.tiny_job.admin.control;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 管理调度执行的全局控制状态，例如临时暂停。
 */
@Component
public class ExecutionControlService {

    private final AtomicBoolean paused = new AtomicBoolean(false);
    private volatile long pausedAt = -1L;

    public boolean pause() {
        boolean changed = paused.compareAndSet(false, true);
        if (changed) {
            pausedAt = System.currentTimeMillis();
        }
        return changed;
    }

    public boolean resume() {
        boolean changed = paused.compareAndSet(true, false);
        if (changed) {
            pausedAt = -1L;
        }
        return changed;
    }

    public boolean isPaused() {
        return paused.get();
    }

    public long getPausedAt() {
        return pausedAt;
    }
}


package com.tiny_job.admin.control;

import com.tiny_job.admin.dao.entity.JobControlState;
import com.tiny_job.admin.dao.mapper.JobControlStateMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 管理调度执行的全局控制状态，例如临时暂停。
 * 状态持久化在数据库中，以保证在多实例部署下的一致性。
 */
@Component
public class ExecutionControlService {

    private static final int STATE_ID = 1;
    private static final long CACHE_TTL_MILLIS = 1_000L;

    private final JobControlStateMapper jobControlStateMapper;
    private final AtomicBoolean cachedPaused = new AtomicBoolean(false);
    private volatile long cachedPausedAt = -1L;
    private volatile long lastRefreshTime = 0L;

    public ExecutionControlService(JobControlStateMapper jobControlStateMapper) {
        this.jobControlStateMapper = jobControlStateMapper;
    }

    @PostConstruct
    public void init() {
        refreshState(true);
    }

    public boolean pause() {
        long now = System.currentTimeMillis();
        int updated = jobControlStateMapper.updatePauseState(STATE_ID, true, now, false);
        if (updated > 0) {
            cachedPaused.set(true);
            cachedPausedAt = now;
            lastRefreshTime = System.currentTimeMillis();
            return true;
        }
        refreshState(true);
        return false;
    }

    public boolean resume() {
        int updated = jobControlStateMapper.updatePauseState(STATE_ID, false, -1L, true);
        if (updated > 0) {
            cachedPaused.set(false);
            cachedPausedAt = -1L;
            lastRefreshTime = System.currentTimeMillis();
            return true;
        }
        refreshState(true);
        return false;
    }

    public boolean isPaused() {
        refreshState(false);
        return cachedPaused.get();
    }

    public long getPausedAt() {
        refreshState(false);
        return cachedPausedAt;
    }

    private void refreshState(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && (now - lastRefreshTime) < CACHE_TTL_MILLIS) {
            return;
        }
        synchronized (this) {
            if (!force && (System.currentTimeMillis() - lastRefreshTime) < CACHE_TTL_MILLIS) {
                return;
            }
            JobControlState state = jobControlStateMapper.selectByPrimaryKey(STATE_ID);
            if (state == null) {
                JobControlState initial = new JobControlState();
                initial.setId(STATE_ID);
                initial.setPaused(false);
                initial.setPausedAt(-1L);
                jobControlStateMapper.insertSelective(initial);
                cachedPaused.set(false);
                cachedPausedAt = -1L;
            } else {
                cachedPaused.set(state.isPaused());
                Long pausedAt = state.getPausedAt();
                cachedPausedAt = pausedAt == null ? -1L : pausedAt;
            }
            lastRefreshTime = System.currentTimeMillis();
        }
    }
}

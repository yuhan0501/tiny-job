package com.tiny_job.admin.controller;

import com.tiny_job.admin.control.ExecutionControlService;
import com.tiny_job.admin.controller.model.CommonResult;
import com.tiny_job.admin.dao.JobInfoHelper;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.dao.entity.JobLog;
import com.tiny_job.admin.dao.mapper.JobLogMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/jobinfo/stats")
public class JobStatisticsController {

    private static final int DEFAULT_WINDOW_DAYS = 7;

    @Resource
    private JobLogMapper jobLogMapper;

    @Resource
    private JobInfoHelper jobInfoHelper;

    @Resource
    private ExecutionControlService executionControlService;

    @GetMapping("/runtime")
    public CommonResult<JobStatisticsResponse> runtimeStats() {
        long now = System.currentTimeMillis();
        Timestamp since = new Timestamp(now - TimeUnit.DAYS.toMillis(DEFAULT_WINDOW_DAYS));

        List<JobLog> logs = jobLogMapper.selectSince(since);

        Map<LocalDate, DailyStat> dailyMap = new TreeMap<>();
        Map<Long, JobAgg> jobAggMap = new HashMap<>();

        long success = 0L;
        long failed = 0L;

        ZoneId zoneId = ZoneId.systemDefault();
        for (JobLog log : logs) {
            boolean ok = log.getHandleCode() != null && log.getHandleCode().startsWith("2");
            LocalDate day = Instant.ofEpochMilli(log.getTriggerTime().getTime()).atZone(zoneId).toLocalDate();
            DailyStat dailyStat = dailyMap.computeIfAbsent(day, d -> new DailyStat(d.toString()));
            if (ok) {
                dailyStat.incrementSuccess();
                success++;
            } else {
                dailyStat.incrementFailed();
                failed++;
            }

            JobAgg agg = jobAggMap.computeIfAbsent(log.getJobId(), id -> new JobAgg(id));
            if (ok) {
                agg.incrementSuccess();
            } else {
                agg.incrementFailed();
            }
        }

        List<DailyStat> dailyStats = new ArrayList<>(dailyMap.values());
        List<JobRanking> topJobs = buildTopJobs(jobAggMap);

        JobStatisticsResponse data = new JobStatisticsResponse();
        data.setPaused(executionControlService.isPaused());
        data.setRunningJobs(jobInfoHelper.countRunningJobs());
        data.setSuccessCount(success);
        data.setFailedCount(failed);
        data.setTotalExecutions(success + failed);
        data.setDailyStats(dailyStats);
        data.setTopJobs(topJobs);

        CommonResult<JobStatisticsResponse> result = new CommonResult<>();
        result.setData(data);
        return result;
    }

    private List<JobRanking> buildTopJobs(Map<Long, JobAgg> jobAggMap) {
        List<JobAgg> aggs = new ArrayList<>(jobAggMap.values());
        aggs.sort(Comparator.comparingLong(JobAgg::getTotal).reversed());
        List<JobRanking> rankings = new ArrayList<>();
        int limit = Math.min(5, aggs.size());
        for (int i = 0; i < limit; i++) {
            JobAgg agg = aggs.get(i);
            JobRanking ranking = new JobRanking();
            ranking.setJobId(agg.getJobId());
            ranking.setSuccess(agg.getSuccess());
            ranking.setFailed(agg.getFailed());
            try {
                JobInfo jobInfo = jobInfoHelper.findJobById(agg.getJobId());
                ranking.setJobDesc(jobInfo.getJobDesc());
            }
            catch (Exception ignored) {
                ranking.setJobDesc("Job " + agg.getJobId());
            }
            rankings.add(ranking);
        }
        return rankings;
    }

    public static class JobStatisticsResponse {
        private boolean paused;
        private long runningJobs;
        private long totalExecutions;
        private long successCount;
        private long failedCount;
        private List<DailyStat> dailyStats = Collections.emptyList();
        private List<JobRanking> topJobs = Collections.emptyList();

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public long getRunningJobs() {
            return runningJobs;
        }

        public void setRunningJobs(long runningJobs) {
            this.runningJobs = runningJobs;
        }

        public long getTotalExecutions() {
            return totalExecutions;
        }

        public void setTotalExecutions(long totalExecutions) {
            this.totalExecutions = totalExecutions;
        }

        public long getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(long successCount) {
            this.successCount = successCount;
        }

        public long getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(long failedCount) {
            this.failedCount = failedCount;
        }

        public List<DailyStat> getDailyStats() {
            return dailyStats;
        }

        public void setDailyStats(List<DailyStat> dailyStats) {
            this.dailyStats = dailyStats;
        }

        public List<JobRanking> getTopJobs() {
            return topJobs;
        }

        public void setTopJobs(List<JobRanking> topJobs) {
            this.topJobs = topJobs;
        }
    }

    public static class DailyStat {
        private final String date;
        private long success;
        private long failed;

        public DailyStat(String date) {
            this.date = date;
        }

        public String getDate() {
            return date;
        }

        public long getSuccess() {
            return success;
        }

        public long getFailed() {
            return failed;
        }

        public long getTotal() {
            return success + failed;
        }

        public void incrementSuccess() {
            this.success++;
        }

        public void incrementFailed() {
            this.failed++;
        }
    }

    public static class JobRanking {
        private Long jobId;
        private String jobDesc;
        private long success;
        private long failed;

        public Long getJobId() {
            return jobId;
        }

        public void setJobId(Long jobId) {
            this.jobId = jobId;
        }

        public String getJobDesc() {
            return jobDesc;
        }

        public void setJobDesc(String jobDesc) {
            this.jobDesc = jobDesc;
        }

        public long getSuccess() {
            return success;
        }

        public void setSuccess(long success) {
            this.success = success;
        }

        public long getFailed() {
            return failed;
        }

        public void setFailed(long failed) {
            this.failed = failed;
        }

        public long getTotal() {
            return success + failed;
        }
    }

    private static class JobAgg {
        private final Long jobId;
        private long success;
        private long failed;

        JobAgg(Long jobId) {
            this.jobId = jobId;
        }

        void incrementSuccess() {
            success++;
        }

        void incrementFailed() {
            failed++;
        }

        long getTotal() {
            return success + failed;
        }

        public Long getJobId() {
            return jobId;
        }

        public long getSuccess() {
            return success;
        }

        public long getFailed() {
            return failed;
        }
    }
}

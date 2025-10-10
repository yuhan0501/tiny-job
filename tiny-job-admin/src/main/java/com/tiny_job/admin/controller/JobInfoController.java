package com.tiny_job.admin.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tiny_job.admin.controller.model.CommonResult;
import com.tiny_job.admin.dao.JobInfoHelper;
import com.tiny_job.admin.dao.entity.JobConfig;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.utils.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-20 13:33
 **/
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {

    private static Logger logger = LoggerFactory.getLogger(JobInfoController.class);
    @Resource
    private JobInfoHelper jobInfoHelper;

    @Autowired
    private HttpServletRequest request;


    @RequestMapping("/list")
    @ResponseBody
    public CommonResult<List<JobInfo>> list(@RequestParam(required = false,defaultValue = "0")int currentPage,
                                            @RequestParam(required = false,defaultValue = "0")int pageSize,
                                            Integer jobStatus,String jobDesc) {
        CommonResult<List<JobInfo>> commonResult = new CommonResult<>();
        Page page = PageHelper.startPage(currentPage,pageSize);
        commonResult.setData(jobInfoHelper.jobInfoList(jobStatus,jobDesc));
        commonResult.setCurrentPage(page.getPageNum());
        commonResult.setPageSize(page.getPageSize());
        commonResult.setTotalRecord(page.getTotal());
        return commonResult;
    }

    @RequestMapping("/add")
    @ResponseBody
    public CommonResult add(JobInfo jobInfo) {
        logger.info(jobInfo.toString());
        CommonResult<List<JobInfo>> commonResult = new CommonResult<>();
        commonResult.setMsg("success");
        return commonResult;
    }

    @PostMapping("/update")
    @ResponseBody
    public CommonResult<Integer> update(@RequestBody JobInfo payload) {
        CommonResult<Integer> result = new CommonResult<>();
        if (payload == null || payload.getId() == null) {
            result.setCode(1);
            result.setMsg("id is required");
            return result;
        }

        JobInfo existing;
        try {
            existing = jobInfoHelper.findJobById(payload.getId());
        }
        catch (Exception e) {
            logger.warn("job not found for id {}", payload.getId(), e);
            result.setCode(1);
            result.setMsg("job not found");
            return result;
        }

        JobInfo toUpdate = new JobInfo();
        toUpdate.setId(existing.getId());

        boolean hasJobInfoChanges = copyNonNullProperties(payload, toUpdate);

        boolean cronChanged = StringUtils.hasText(payload.getJobCron()) &&
                !payload.getJobCron().equals(existing.getJobCron());
        boolean resumeRequested = payload.getJobStatus() != null && payload.getJobStatus() == 1 &&
                (existing.getJobStatus() == null || existing.getJobStatus() != 1);

        if (cronChanged || resumeRequested) {
            String cron = StringUtils.hasText(payload.getJobCron()) ? payload.getJobCron() : existing.getJobCron();
            try {
                CronExpression cronExpression = new CronExpression(cron);
                Date next = cronExpression.getNextValidTimeAfter(new Date());
                if (next != null) {
                    long now = System.currentTimeMillis();
                    toUpdate.setTriggerLastTime(now);
                    toUpdate.setTriggerNextTime(next.getTime());
                    hasJobInfoChanges = true;
                }
            }
            catch (ParseException e) {
                logger.warn("invalid cron expression provided: {}", cron, e);
                result.setCode(1);
                result.setMsg("invalid cron expression");
                return result;
            }
        }

        int effected = 0;
        if (hasJobInfoChanges) {
            toUpdate.setUpdateTime(existing.getUpdateTime());
            effected = jobInfoHelper.updateSelective(toUpdate);
        }

        int jobConfigUpdated = 0;
        JobConfig incomingConfig = payload.getJobConfig();
        JobConfig existingConfig = existing.getJobConfig();
        String resolvedJobType = StringUtils.hasText(payload.getJobType()) ? payload.getJobType() : existing.getJobType();
        if (incomingConfig != null) {
            if (existingConfig != null) {
                if (incomingConfig.getId() == null) {
                    incomingConfig.setId(existingConfig.getId());
                }
                if (!StringUtils.hasText(incomingConfig.getServiceType())) {
                    incomingConfig.setServiceType(resolvedJobType);
                }
                jobConfigUpdated = jobInfoHelper.updateJobConfigSelective(incomingConfig);
            }
        }
        else if (existingConfig != null && StringUtils.hasText(resolvedJobType) && !resolvedJobType.equals(existingConfig.getServiceType())) {
            JobConfig serviceTypeUpdater = new JobConfig();
            serviceTypeUpdater.setId(existingConfig.getId());
            serviceTypeUpdater.setServiceType(resolvedJobType);
            jobConfigUpdated = jobInfoHelper.updateJobConfigSelective(serviceTypeUpdater);
        }

        int affectedTotal = effected + jobConfigUpdated;
        result.setData(affectedTotal);
        if (affectedTotal > 0) {
            result.setMsg("success");
        }
        else {
            result.setCode(1);
            result.setMsg("no row updated");
        }
        return result;
    }

    private boolean copyNonNullProperties(JobInfo source, JobInfo target) {
        boolean changed = false;
        if (source.getJobCron() != null) {
            target.setJobCron(source.getJobCron());
            changed = true;
        }
        if (source.getJobDesc() != null) {
            target.setJobDesc(source.getJobDesc());
            changed = true;
        }
        if (source.getAuthor() != null) {
            target.setAuthor(source.getAuthor());
            changed = true;
        }
        if (source.getJobType() != null) {
            target.setJobType(source.getJobType());
            changed = true;
        }
        if (source.getConfigId() != null) {
            target.setConfigId(source.getConfigId());
            changed = true;
        }
        if (source.getExecuteBlockStrategy() != null) {
            target.setExecuteBlockStrategy(source.getExecuteBlockStrategy());
            changed = true;
        }
        if (source.getExecuteTimeout() != null) {
            target.setExecuteTimeout(source.getExecuteTimeout());
            changed = true;
        }
        if (source.getExecuteFailRetryCount() != null) {
            target.setExecuteFailRetryCount(source.getExecuteFailRetryCount());
            changed = true;
        }
        if (source.getChildJobId() != null) {
            target.setChildJobId(source.getChildJobId());
            changed = true;
        }
        if (source.getJobStatus() != null) {
            target.setJobStatus(source.getJobStatus());
            changed = true;
        }
        if (source.getTriggerLastTime() != null) {
            target.setTriggerLastTime(source.getTriggerLastTime());
            changed = true;
        }
        if (source.getTriggerNextTime() != null) {
            target.setTriggerNextTime(source.getTriggerNextTime());
            changed = true;
        }
        if (source.getJobVersion() != null) {
            target.setJobVersion(source.getJobVersion());
            changed = true;
        }
        return changed;
    }
}

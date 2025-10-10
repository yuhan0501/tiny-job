package com.tiny_job.admin.controller;

import com.tiny_job.admin.control.ExecutionControlService;
import com.tiny_job.admin.controller.model.CommonResult;
import com.tiny_job.admin.dao.JobInfoHelper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/control")
public class JobControlController {

    @Resource
    private ExecutionControlService executionControlService;

    @Resource
    private JobInfoHelper jobInfoHelper;

    @GetMapping("/pause")
    public CommonResult<Map<String, Object>> pauseStatus() {
        CommonResult<Map<String, Object>> result = new CommonResult<>();
        Map<String, Object> data = new HashMap<>();
        data.put("paused", executionControlService.isPaused());
        data.put("pausedAt", executionControlService.getPausedAt());
        data.put("runningCount", jobInfoHelper.countRunningJobs());
        result.setData(data);
        return result;
    }

    @PostMapping("/pause")
    public CommonResult<Boolean> pauseAll() {
        CommonResult<Boolean> result = new CommonResult<>();
        boolean changed = executionControlService.pause();
        result.setData(true);
        result.setMsg(changed ? "paused" : "already paused");
        return result;
    }

    @DeleteMapping("/pause")
    public CommonResult<Boolean> resumeAll() {
        CommonResult<Boolean> result = new CommonResult<>();
        boolean changed = executionControlService.resume();
        if (!changed) {
            result.setMsg("already resumed");
        }
        result.setData(true);
        return result;
    }
}

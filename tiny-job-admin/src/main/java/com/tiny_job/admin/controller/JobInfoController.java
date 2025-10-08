package com.tiny_job.admin.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tiny_job.admin.controller.model.CommonResult;
import com.tiny_job.admin.dao.JobInfoHelper;
import com.tiny_job.admin.dao.entity.JobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    public CommonResult<List<JobInfo>> list(@RequestParam(required = false,defaultValue = "1")int currentPage,
                                            @RequestParam(required = false,defaultValue = "20")int pageSize,
                                            Integer jobStatus,String jobDesc,String jobType) {
        CommonResult<List<JobInfo>> commonResult = new CommonResult<>();
        currentPage = currentPage <= 0 ? 1 : currentPage;
        pageSize = pageSize <= 0 ? 20 : pageSize;
        Page page = PageHelper.startPage(currentPage,pageSize);
        commonResult.setData(jobInfoHelper.jobInfoList(jobStatus,jobDesc,jobType));
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

    @RequestMapping(value = "/pause", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<Void> pause(@RequestParam("id") Long jobId) {
        CommonResult<Void> result = new CommonResult<>();
        if (jobId == null) {
            result.setCode(1);
            result.setMsg("job id can not be null");
            return result;
        }
        try {
            boolean success = jobInfoHelper.pauseJob(jobId);
            if (success) {
                result.setMsg("pause success");
            }
            else {
                result.setCode(1);
                result.setMsg("pause job failed");
            }
        }
        catch (Exception e) {
            logger.error("pause job {} error", jobId, e);
            result.setCode(1);
            result.setMsg("pause job error: " + e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/resume", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<Void> resume(@RequestParam("id") Long jobId) {
        CommonResult<Void> result = new CommonResult<>();
        if (jobId == null) {
            result.setCode(1);
            result.setMsg("job id can not be null");
            return result;
        }
        try {
            boolean success = jobInfoHelper.resumeJob(jobId);
            if (success) {
                result.setMsg("resume success");
            }
            else {
                result.setCode(1);
                result.setMsg("resume job failed");
            }
        }
        catch (Exception e) {
            logger.error("resume job {} error", jobId, e);
            result.setCode(1);
            result.setMsg("resume job error: " + e.getMessage());
        }
        return result;
    }
}

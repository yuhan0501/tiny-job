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
}

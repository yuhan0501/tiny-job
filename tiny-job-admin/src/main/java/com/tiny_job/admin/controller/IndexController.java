package com.tiny_job.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-20 08:49
 **/
//@Controller
public class IndexController {

    @RequestMapping({"", "index.html"})
    public String index() {
        return "index";
    }

    @RequestMapping({"/jobinfo", "index.html"})
    public String jobinfo() {
        return "jobinfo/index";
    }

}

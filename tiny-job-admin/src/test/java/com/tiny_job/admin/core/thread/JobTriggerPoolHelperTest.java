package com.tiny_job.admin.core.thread;

import com.tiny_job.TinyJobApplicaiton;
import com.tiny_job.admin.dao.JobInfoHelper;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.utils.CronExpression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-17 08:39
 **/
@SpringBootTest(classes = {TinyJobApplicaiton.class})
public class JobTriggerPoolHelperTest {
    @Autowired
    private JobTriggerPoolHelper jobTriggerPoolHelper;
    @Resource
    private JobInfoHelper jobInfoHelper;

    @BeforeEach
    public void init() {
        jobTriggerPoolHelper.init();
    }

    @ParameterizedTest
    @CsvSource("1")
    public void triggerJob(Long id) throws InterruptedException {
        JobInfo jobInfo = jobInfoHelper.findJobById(id);
        jobTriggerPoolHelper.triggerJob(jobInfo, -100);
    }

    @ParameterizedTest
    @CsvSource("1")
    public void tgTest(Long id) throws ParseException {
        JobInfo jobInfo = jobInfoHelper.findJobById(id);
        jobInfo.setJobCron("0/3 * * * * ? *");
        jobInfo.setTriggerNextTime(1587259231000L);
        long nowTime = 1587261975353L;

        System.out.println("triggerjob11" + jobInfo.getTriggerNextTime());

        refreshNextValidTime(jobInfo, new Date(nowTime));

        checkHighFrequency(jobInfo, nowTime);
    }

    /**
     * 如果是超频繁任务，递归生成最近一个周期内的所有触发任务
     *
     * @param jobInfo
     */
    public void checkHighFrequency(JobInfo jobInfo, Long nowTime) throws ParseException {

        if (jobInfo.getTriggerNextTime() < (nowTime + 5000)) {
            //更新下次调度job的时间
            refreshNextValidTime(jobInfo, new Date(jobInfo.getTriggerNextTime()));
            System.out.println("triggerjob" + jobInfo.getTriggerNextTime());
            //将任务放入待执行队列
            //判断是否是超高频繁任务，即调度周期小于5s一次
            checkHighFrequency(jobInfo, nowTime);
        }
    }

    public void refreshNextValidTime(JobInfo cron, Date fromTime) throws ParseException {
        Date nextValidTime = new CronExpression(cron.getJobCron()).getNextValidTimeAfter(fromTime);
        if (nextValidTime != null) {
            cron.setTriggerLastTime(cron.getTriggerNextTime());
            cron.setTriggerNextTime(nextValidTime.getTime());
            System.out.println(nextValidTime.getTime());
        }
        else {
        }
    }

    @AfterEach
    public void shutdown() {
        jobTriggerPoolHelper.shutdown();
    }

}

package com.tiny_job.admin.core.dao;

import com.tiny_job.TinyJobApplicaiton;
import com.tiny_job.admin.dao.JobInfoHelper;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.dao.mapper.JobInfoMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-17 22:34
 **/
@SpringBootTest(classes = {TinyJobApplicaiton.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class JobInfoHelperTest {

    @Resource
    private JobInfoHelper jobInfoHelper;

    @Resource
    private JobInfoMapper jobInfoMapper;


    @Test
    public void scheduleJobQuery() {
        jobInfoHelper.scheduleJobQuery(System.currentTimeMillis(), 10);
    }

    @Test
    public void versionTest() {
        JobInfo jobInfo = jobInfoMapper.selectByPrimaryKey(1L);
        System.out.println(jobInfoMapper.updateByPrimaryKey(jobInfo));
    }
}

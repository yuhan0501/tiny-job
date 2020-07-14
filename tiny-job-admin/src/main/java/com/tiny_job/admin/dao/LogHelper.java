package com.tiny_job.admin.dao;

import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.dao.entity.JobLog;
import com.tiny_job.admin.dao.mapper.JobLogMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-05-19 13:38
 **/
@Repository
public class LogHelper {

    private Logger logger = LoggerFactory.getLogger(LogHelper.class);

    @Resource
    private JobLogMapper jobLogMapper;

    @Transactional
    public void logSuccess(JobInfo jobInfo) {
        saveLog(jobInfo, "success", "200");
    }


    public void saveLog(JobInfo jobInfo, String msg, String code) {
        JobLog jobLog = new JobLog();
        BeanUtils.copyProperties(jobInfo, jobLog);
        BeanUtils.copyProperties(jobInfo.getJobConfig(), jobLog);
        jobLog.setId(null);
        jobLog.setJobId(jobInfo.getId());
        jobLog.setHandleCode(code);
        jobLog.setHandleMsg(msg);
        jobLog.setTriggerTime(new Timestamp(jobInfo.getTriggerLastTime()));
        jobLogMapper.insert(jobLog);
    }

    @Transactional
    public void saveLog(JobInfo jobInfo, CloseableHttpResponse result) {

        String handelCode = result.getStatusLine().getStatusCode() + "";
        String handelMsg = resp2String(result.getEntity());
        saveLog(jobInfo, handelMsg, handelCode);
    }

    private String resp2String(HttpEntity httpEntity) {
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
        }
        catch (IOException e) {
            logger.error("get resp error:{}", e);
            return "";
        }
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        }
        catch (IOException e) {
            logger.error("get resp body error.", e);
        }
        return result.toString();
    }

}

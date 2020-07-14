package com.tiny_job.admin.executor;

import com.tiny_job.admin.dao.LogHelper;
import com.tiny_job.admin.dao.entity.JobConfig;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.dao.mapper.JobConfigMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;



/**
 * @description:调用spring微服务
 * @author: yuhan
 * @create: 2020-04-16 21:30
 **/
@Component("http")
public class TinyJobSpringHttpAdapter implements TinyJobExecutorBaseAdapter {
    private static Logger logger = LoggerFactory.getLogger(TinyJobSpringHttpAdapter.class);

    @Resource
    private JobConfigMapper jobConfigMapper;
    @Resource
    private LogHelper logHelper;


    @Override
    public void processJob(JobInfo jobInfo) {
        jobInfo.setJobConfig(jobConfigMapper.selectByPrimaryKey(jobInfo.getConfigId()));
        JobConfig jobConfig = jobInfo.getJobConfig();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(jobConfig.getExecuteService() + "/" + jobConfig.getExecuteMethod() +
                "?" + jobConfig.getExecuteParam());
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(0).build();

        httpPost.setConfig(requestConfig);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            logHelper.saveLog(jobInfo, response);
        }
        catch (IOException e) {
            logger.error("call service error:{}", e);
            logHelper.saveLog(jobInfo, "500", e.getMessage());
        }
    }


}

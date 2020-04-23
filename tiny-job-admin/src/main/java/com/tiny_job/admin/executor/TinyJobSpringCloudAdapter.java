package com.tiny_job.admin.executor;

import com.tiny_job.admin.dao.entity.JobConfig;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.dao.entity.JobLog;
import com.tiny_job.admin.dao.mapper.JobConfigMapper;
import com.tiny_job.admin.dao.mapper.JobLogMapper;
import com.tiny_job.admin.exception.TinyJobException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

/**
 * @description:调用spring微服务
 * @author: yuhan
 * @create: 2020-04-16 21:30
 **/
@Component("spring_cloud")
public class TinyJobSpringCloudAdapter implements TinyJobExecutorBaseAdapter {
    private static Logger logger = LoggerFactory.getLogger(TinyJobSpringCloudAdapter.class);

    @Resource
    private JobConfigMapper jobConfigMapper;
    @Resource
    private JobLogMapper jobLogMapper;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public void processJob(JobInfo jobInfo) {
        jobInfo.setJobConfig(jobConfigMapper.selectByPrimaryKey(jobInfo.getConfigId()));
        JobConfig jobConfig = jobInfo.getJobConfig();
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances(jobConfig.getExecuteService());
        if (CollectionUtils.isEmpty(serviceInstanceList)) {
            throw new TinyJobException("there is no available services.");
        }
        ServiceInstance serviceInstance = getRandomInstrance(serviceInstanceList);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(serviceInstance.getUri() + "/" + jobConfig.getExecuteMethod() +
                "?" + jobConfig.getExecuteParam());
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(0).build();

        httpPost.setConfig(requestConfig);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            logResult(response, jobInfo);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServiceInstance getRandomInstrance(List<ServiceInstance> serviceInstanceList) {
        Random random = new Random();
        return serviceInstanceList.get(random.nextInt(serviceInstanceList.size()));
    }

    private void logResult(CloseableHttpResponse result, JobInfo jobInfo) throws IOException {
        JobLog jobLog = new JobLog();
        BeanUtils.copyProperties(jobInfo, jobLog);
        BeanUtils.copyProperties(jobInfo.getJobConfig(), jobLog);
        jobLog.setId(null);
        jobLog.setJobId(jobInfo.getId());
        jobLog.setHandleCode(result.getStatusLine().getStatusCode() + "");
        jobLog.setHandleMsg(resp2String(result.getEntity().getContent()));
        jobLog.setTriggerTime(new Timestamp(jobInfo.getTriggerLastTime()));
//        jobLog.setHandleCode

        jobLogMapper.insert(jobLog);
    }

    private String resp2String(InputStream inputStream) {
        StringBuffer result = new StringBuffer();
        try {
            String line = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        }
        catch (IOException e) {
            logger.error("get resp body error.", e);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                logger.warn("close inputStream error");
            }
        }
        return result.toString();
    }
}

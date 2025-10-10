package com.tiny_job.admin.executor;

import com.tiny_job.admin.dao.LogHelper;
import com.tiny_job.admin.dao.entity.JobConfig;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.dao.mapper.JobConfigMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.entity.StringEntity;



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

        String requestUrl = jobConfig.getExecuteService();
        if (!StringUtils.hasText(requestUrl)) {
            logger.warn("executeService is blank for job {}", jobInfo.getId());
            return;
        }
        String requestMethod = StringUtils.hasText(jobConfig.getExecuteMethod()) ? jobConfig.getExecuteMethod().toUpperCase() : "POST";
        String requestParam = jobConfig.getExecuteParam();

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(0).build();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpUriRequest request = buildRequest(requestMethod, requestUrl, requestParam);
            if (request instanceof HttpRequestBase) {
                ((HttpRequestBase) request).setConfig(requestConfig);
            }
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                logHelper.saveLog(jobInfo, response);
            }
        }
        catch (IOException e) {
            logger.error("call service error:{}", e);
            logHelper.saveLog(jobInfo, "500", e.getMessage());
        }
    }

    private HttpUriRequest buildRequest(String method, String url, String params) {
        String normalizedMethod = StringUtils.hasText(method) ? method.toUpperCase() : "POST";
        switch (normalizedMethod) {
            case "GET":
                return new HttpGet(appendQuery(url, params));
            case "DELETE":
                return new HttpDelete(appendQuery(url, params));
            case "PUT": {
                HttpPut httpPut = new HttpPut(url);
                attachEntity(httpPut, params);
                return httpPut;
            }
            case "PATCH": {
                HttpPatch httpPatch = new HttpPatch(url);
                attachEntity(httpPatch, params);
                return httpPatch;
            }
            case "POST":
            default: {
                HttpPost httpPost = new HttpPost(url);
                attachEntity(httpPost, params);
                return httpPost;
            }
        }
    }

    private String appendQuery(String url, String params) {
        if (!StringUtils.hasText(params)) {
            return url;
        }
        if (url.contains("?")) {
            return url + "&" + params;
        }
        return url + "?" + params;
    }

    private void attachEntity(HttpEntityEnclosingRequestBase request, String params) {
        if (!StringUtils.hasText(params)) {
            return;
        }
        request.setEntity(new StringEntity(params, StandardCharsets.UTF_8));
    }


}

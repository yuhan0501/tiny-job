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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
    private LogHelper logHelper;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public void processJob(JobInfo jobInfo) {
        jobInfo.setJobConfig(jobConfigMapper.selectByPrimaryKey(jobInfo.getConfigId()));
        JobConfig jobConfig = jobInfo.getJobConfig();

        if (jobConfig == null) {
            logger.warn("job {} has no configuration", jobInfo.getId());
            return;
        }
        if (!StringUtils.hasText(jobConfig.getExecuteService())) {
            logHelper.saveLog(jobInfo, "serviceId is blank", "400");
            return;
        }
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances(jobConfig.getExecuteService());
        if (CollectionUtils.isEmpty(serviceInstanceList)) {
            logHelper.saveLog(jobInfo, "there is no available services.", "500");
            return;
        }
        ServiceInstance serviceInstance = getRandomInstrance(serviceInstanceList);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpUriRequest request = buildRequest(serviceInstance, jobConfig);
            if (request == null) {
                logHelper.saveLog(jobInfo, "invalid request configuration", "400");
                return;
            }
            if (request instanceof HttpRequestBase) {
                ((HttpRequestBase) request).setConfig(defaultRequestConfig());
            }
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                logHelper.saveLog(jobInfo, response);
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("call service error:{}", e.getMessage(), e);
            logHelper.saveLog(jobInfo, "500", e.getMessage());
        }
    }

    private ServiceInstance getRandomInstrance(List<ServiceInstance> serviceInstanceList) {
        return serviceInstanceList.get(ThreadLocalRandom.current().nextInt(serviceInstanceList.size()));
    }

    private HttpUriRequest buildRequest(ServiceInstance instance, JobConfig jobConfig) throws URISyntaxException {
        String path = jobConfig.getExecutePath();
        if (!StringUtils.hasText(path)) {
            path = "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        URI baseUri = instance.getUri();
        URI targetUri = baseUri.resolve(path);

        String method = StringUtils.hasText(jobConfig.getExecuteMethod()) ?
                jobConfig.getExecuteMethod().toUpperCase() : "POST";
        String params = jobConfig.getExecuteParam();

        switch (method) {
            case "GET":
                return new HttpGet(appendQuery(targetUri, params));
            case "DELETE":
                return new HttpDelete(appendQuery(targetUri, params));
            case "PUT": {
                HttpPut request = new HttpPut(targetUri);
                attachEntity(request, params);
                return request;
            }
            case "PATCH": {
                HttpPatch request = new HttpPatch(targetUri);
                attachEntity(request, params);
                return request;
            }
            case "POST":
            default: {
                HttpPost request = new HttpPost(targetUri);
                attachEntity(request, params);
                return request;
            }
        }
    }

    private RequestConfig defaultRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(5_000)
                .setSocketTimeout(10_000)
                .build();
    }

    private URI appendQuery(URI uri, String params) throws URISyntaxException {
        if (!StringUtils.hasText(params)) {
            return uri;
        }
        StringBuilder sb = new StringBuilder(uri.toString());
        if (uri.toString().contains("?")) {
            sb.append('&').append(params);
        } else {
            sb.append('?').append(params);
        }
        return new URI(sb.toString());
    }

    private void attachEntity(HttpEntityEnclosingRequestBase request, String params) {
        if (!StringUtils.hasText(params)) {
            return;
        }
        request.setEntity(new StringEntity(params, java.nio.charset.StandardCharsets.UTF_8));
    }
}

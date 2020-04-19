package com.tiny_job.admin.executor;

import com.tiny_job.admin.dao.SpringCloudJobConfigDao;
import com.tiny_job.admin.dao.entity.JobInfo;
import com.tiny_job.admin.dao.entity.JobSpringCloudConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;

/**
 * @description:调用spring微服务
 * @author: yuhan
 * @create: 2020-04-16 21:30
 **/
@Component("spring_cloud")
public class TinyJobSpringCloudAdapter implements TinyJobExecutorBaseAdapter {
    private static Logger logger = LoggerFactory.getLogger(TinyJobSpringCloudAdapter.class);

    @Resource
    private SpringCloudJobConfigDao cloudJobConfigDao;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void processJob(JobInfo jobInfo) {
        jobInfo.setJobConfig(cloudJobConfigDao.findByJobId(jobInfo.getId()));
        JobSpringCloudConfig springCloudConfig = (JobSpringCloudConfig) jobInfo.getJobConfig();
        ObjectMapper objectMapper = new ObjectMapper();
        RequestEntity request = RequestEntity
                .post(UriComponentsBuilder.fromUriString("http://" + springCloudConfig.getExecuteService() + springCloudConfig.getExecutePath()).build().toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .body(springCloudConfig.getExecuteParam());
        ResponseEntity<String> reslult = restTemplate.exchange(request, String.class);
        logger.info(reslult.getBody());
    }
}

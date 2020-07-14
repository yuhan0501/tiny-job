package com.tiny_job.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @description:
 * @author: yuhan
 * @create: 2020-04-16 20:23
 **/
@Configuration
@ConfigurationProperties(prefix = "tiny-job")
public class TinyJobConfig {

    private Integer triggerPollSize;


    @Bean
    //客户端负载均衡
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public Integer getTriggerPollSize() {
        return triggerPollSize;
    }

    public void setTriggerPollSize(Integer triggerPollSize) {
        this.triggerPollSize = triggerPollSize;
    }
}

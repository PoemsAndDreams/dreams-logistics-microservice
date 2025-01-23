package com.dreams.logistics.config;

import com.aliyun.oss.OSSClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oss.client")
@Data
public class AliyunConfig {
 
    private String endpoint;
    private String accessKey;
    private String accessKeySecret;
    private String bucketName;
    private String urlPrefix;
 
    @Bean
    public OSSClient oSSClient() {
        return new OSSClient(endpoint, accessKey, accessKeySecret);
    }
 
}
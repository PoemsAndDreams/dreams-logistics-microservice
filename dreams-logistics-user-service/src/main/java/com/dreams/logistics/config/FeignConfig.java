package com.dreams.logistics.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
            10000, // 连接超时（毫秒）
            10000 // 读取超时（毫秒）
        );
    }
}

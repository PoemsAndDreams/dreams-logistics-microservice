package com.dreams.logistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.dreams.logistics.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.dreams.logistics")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.dreams.logistics.service"})
public class DreamsLogisticsUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DreamsLogisticsUserServiceApplication.class, args);
	}

}

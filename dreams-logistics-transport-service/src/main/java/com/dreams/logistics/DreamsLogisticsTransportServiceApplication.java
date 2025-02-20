package com.dreams.logistics;

import com.dreams.logistics.config.FeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.dreams.logistics.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.dreams.logistics")
@EnableDiscoveryClient
@EnableNeo4jRepositories(basePackages = "com.dreams.logistics")
@EnableFeignClients(basePackages = {"com.dreams.logistics"},defaultConfiguration = FeignConfig.class)
public class DreamsLogisticsTransportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DreamsLogisticsTransportServiceApplication.class, args);
	}

}

package com.dreams.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//解决Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
//Reason: Failed to determine a suitable driver class
//排除数据库exclude = {DataSourceAutoConfiguration.class}
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class DreamsLogisticsGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(DreamsLogisticsGatewayApplication.class, args);
	}

}

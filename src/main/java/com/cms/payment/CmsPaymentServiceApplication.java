package com.cms.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class CmsPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmsPaymentServiceApplication.class, args);
	}

}

package com.waregang.receiving_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableConfigurationProperties(JwtProperties.class)
public class ReceivingServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReceivingServiceApplication.class, args);
	}

}

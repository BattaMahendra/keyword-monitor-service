package com.mahi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Keyword Monitor & Notification Service
 */
@SpringBootApplication
@EnableScheduling
public class KeywordCheckHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeywordCheckHubApplication.class, args);
	}

}

package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class HelloBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloBackendApplication.class, args);
	}
}

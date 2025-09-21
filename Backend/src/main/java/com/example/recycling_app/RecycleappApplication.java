package com.example.recycling_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


// Spring Boot 애플리케이션의 진입점 (main 메서드를 통해 실행됨)
@SpringBootApplication
@EnableScheduling
public class RecycleappApplication {

	// 애플리케이션 실행 시작 지점
	public static void main(String[] args) {
		SpringApplication.run(RecycleappApplication.class, args);
	}
}
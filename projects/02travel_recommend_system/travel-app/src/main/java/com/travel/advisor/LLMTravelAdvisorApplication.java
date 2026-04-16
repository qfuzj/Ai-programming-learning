package com.travel.advisor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@MapperScan("com.travel.advisor.mapper")
public class LLMTravelAdvisorApplication {

    public static void main(String[] args) {
        SpringApplication.run(LLMTravelAdvisorApplication.class, args);
    }
}

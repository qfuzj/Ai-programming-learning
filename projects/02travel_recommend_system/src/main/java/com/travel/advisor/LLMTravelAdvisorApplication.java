package com.travel.advisor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.travel.advisor.mapper")
public class LLMTravelAdvisorApplication {

    public static void main(String[] args) {
        SpringApplication.run(LLMTravelAdvisorApplication.class, args);
    }
}

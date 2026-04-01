package com.campus.resourcesharing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campus.resourcesharing.mapper")
public class CampusResourceSharingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusResourceSharingApplication.class, args);
    }
}

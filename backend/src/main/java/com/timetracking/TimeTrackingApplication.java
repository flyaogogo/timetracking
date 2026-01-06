package com.timetracking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.timetracking.mapper")
public class TimeTrackingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TimeTrackingApplication.class, args);
    }
}
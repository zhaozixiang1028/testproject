package com.company.dailywork;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.company.dailywork.mapper")
public class DailyWorkApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyWorkApplication.class, args);
    }
}

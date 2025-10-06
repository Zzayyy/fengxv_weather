package com.zzay.fengxv_weather;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class FengxvWeatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(FengxvWeatherApplication.class, args);
    }

}

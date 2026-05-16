package com.carbon.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.carbon.platform.mapper")
public class CarbonPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarbonPlatformApplication.class, args);
    }
}

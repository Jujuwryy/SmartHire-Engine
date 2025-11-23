package com.george;

import com.george.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy
@EnableConfigurationProperties(AppProperties.class)
public class SmartHireApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SmartHireApplication.class, args);
    }
}


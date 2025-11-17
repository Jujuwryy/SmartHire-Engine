package com.george;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SmartHireApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SmartHireApplication.class, args);
    }
}


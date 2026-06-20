package com.example.ecomm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EcommApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommApplication.class, args);
    }
}

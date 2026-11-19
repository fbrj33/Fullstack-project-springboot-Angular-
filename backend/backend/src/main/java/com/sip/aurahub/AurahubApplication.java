package com.sip.aurahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AurahubApplication {
    public static void main(String[] args) {
        SpringApplication.run(AurahubApplication.class, args);
    }
}

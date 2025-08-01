package com.dataox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataOxScrapingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataOxScrapingApplication.class, args);
    }

}

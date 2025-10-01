package com.excycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan("com.excycle.filter")
public class ExcycleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExcycleApplication.class, args);
    }
}
package com.excycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ServletComponentScan("com.excycle.filter")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
public class ExcycleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExcycleApplication.class, args);
    }
}
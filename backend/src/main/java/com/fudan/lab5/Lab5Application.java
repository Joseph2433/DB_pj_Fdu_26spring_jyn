package com.fudan.lab5;

import com.fudan.lab5.ai.DeepSeekProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DeepSeekProperties.class)
public class Lab5Application {
    public static void main(String[] args) {
        SpringApplication.run(Lab5Application.class, args);
    }
}

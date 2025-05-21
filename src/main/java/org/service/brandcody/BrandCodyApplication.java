package org.service.brandcody;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class BrandCodyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrandCodyApplication.class, args);
    }

}

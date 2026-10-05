package com.openlabmx.claudinary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
        scanBasePackages = {"com.openlabmx.claudinary"}
)
@EnableJpaRepositories(basePackages = {"com.openlabmx.claudinary.repository"})
@EnableTransactionManagement
@EnableAsync
public class ClaudinaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaudinaryApplication.class, args);
    }
}

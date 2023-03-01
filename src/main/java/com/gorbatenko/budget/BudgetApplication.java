package com.gorbatenko.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories("com.gorbatenko.budget.repository")
public class BudgetApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(BudgetApplication.class, args);
    }

}


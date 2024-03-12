package com.gorbatenko.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class BudgetApplication extends SpringBootServletInitializer {

    // for launch using Intellij Idea need to add "VM options" end put there "--add-opens=java.base/java.time=ALL-UNNAMED"
    public static void main(String[] args) {
        SpringApplication.run(BudgetApplication.class, args);
    }

}


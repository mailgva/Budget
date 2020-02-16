package com.gorbatenko.budget;

import com.gorbatenko.budget.config.backupdatabase.BackupDBConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class BudgetApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(BudgetApplication.class, args);

    }

    @PostConstruct
    public void init(){
        BackupDBConfig config = new BackupDBConfig();
        config.createBackupDataBase();
    }
}


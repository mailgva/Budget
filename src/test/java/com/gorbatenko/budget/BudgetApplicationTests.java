package com.gorbatenko.budget;

import com.gorbatenko.budget.repository.BudgetRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BudgetApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    private BudgetRepository budgetRepository;

    @Test
    public void testGetByDate() {
        /*
        TimeZone timeZone = TimeZone.getDefault();
        long hours = TimeUnit.MILLISECONDS.toHours(timeZone.getOffset(System.currentTimeMillis()));
        LocalDateTime ldt = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN).plusHours(hours);
        System.out.println(ldt);
        budgetRepository.getBudgetByDateAndUser_Group(ldt, "5d09fea5141c180004c77965").forEach(System.out::println);
        budgetRepository.findAllByDate(ldt).forEach(System.out::println);
        System.out.println("================");
        budgetRepository.findAll().forEach(System.out::println);*/
    }

}

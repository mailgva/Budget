package com.gorbatenko.budget;

import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.ItemRepository;
import com.gorbatenko.budget.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@SpringBootApplication
public class BudgetApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(BudgetApplication.class, args);
    }


    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;


    @PostConstruct
    public void init() {
        budgetRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        User user1 = new User("Vladimir", "mail@gmail.com");
        user1 = userRepository.saveUser(user1);

        User user2 = new User("Yana", "mail@ya.ru");
        user2.setGroup(user1.getGroup());
        user2 = userRepository.saveUser(user2);

        User user3 = new User("Test", "test@gmail.com");
        user3 = userRepository.saveUser(user3);


        Item item = new Item("Basic ZP");
        item = itemRepository.save(item);

        Item item1 = new Item("Eda");
        item1 = itemRepository.save(item1);

        Item item2 = new Item("Transport");
        item2 = itemRepository.save(item2);

        Budget budget = new Budget(user1, Type.PROFIT, item, LocalDate.now(), "ZP", 1000.0);
        Budget budget1 = new Budget(user1, Type.SPENDING, item1, LocalDate.now(), "broad", 9.0);
        Budget budget2 = new Budget(user1, Type.SPENDING, item1, LocalDate.now(),"buter", 10.0);
        Budget budget3 = new Budget(user2, Type.SPENDING, item2, LocalDate.now(),"Fuel", 500.0);

        Budget budget4 = new Budget(user3, Type.SPENDING, item2, LocalDate.now(),"Diesel", 220.0);

        budgetRepository.saveBudget(budget);
        budgetRepository.saveBudget(budget3);
        budgetRepository.saveBudget(budget1);
        budgetRepository.saveBudget(budget2);

        budgetRepository.saveBudget(budget4);


        for (Budget b : budgetRepository.findAll()) {
            System.out.println(b);
        }

    }
}

package com.gorbatenko.budget;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Item;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.ItemRepository;
import com.gorbatenko.budget.repository.UserGroupRepository;
import com.gorbatenko.budget.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;

@SpringBootApplication
public class BudgetApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(BudgetApplication.class, args);
        /*TEST DATA*/
        /*BudgetRepository budgetRepository = ctx.getBean(BudgetRepository.class);
        ItemRepository itemRepository = ctx.getBean(ItemRepository.class);
        UserRepository userRepository = ctx.getBean(UserRepository.class);
        UserGroupRepository userGroupRepository = ctx.getBean(UserGroupRepository.class);

        budgetRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();


        User user = new User(null, "Vladimir", "mail@gmail.com");
        userRepository.save(user);

        for(User u : userRepository.findAll()) {
            System.out.println(u);
        }

        Item item = new Item("Basic ZP");
        item = itemRepository.save(item);

        Item item1 = new Item("Eda");
        item1 = itemRepository.save(item1);

        Item item2 = new Item("Transport");
        item2 = itemRepository.save(item2);

        Budget budget = new Budget(user, Type.PROFIT, item, LocalDate.now(), "ZP", 1000.0);
        Budget budget1 = new Budget(user, Type.SPENDING, item1, LocalDate.now(), "broad", 8.0);
        Budget budget2 = new Budget(user, Type.SPENDING, item1, LocalDate.now(),"buter", 10.0);
        Budget budget3 = new Budget(user, Type.SPENDING, item2, LocalDate.now(),"Fuel", 500.0);

        budgetRepository.save(budget);
        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);

        for (Budget b : budgetRepository.findAll()) {
            System.out.println(b);
        }*/
    }

}

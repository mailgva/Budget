package com.gorbatenko.budget;

import static com.gorbatenko.budget.util.UserUtil.prepareToSave;

import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.repository.UserRepository;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BudgetApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(BudgetApplication.class, args);
    }


    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private KindRepository kindRepository;
    @Autowired
    private UserRepository userRepository;



    @PostConstruct
    public void init() {

        budgetRepository.deleteAll();
        kindRepository.deleteAll();
        userRepository.deleteAll();

        Kind kind = new Kind(Type.PROFIT, "Зарплата");
        kind = kindRepository.save(kind);

        Kind kind0 = new Kind(Type.PROFIT, "Доп. доход");
        kind0 = kindRepository.save(kind0);

        Kind kind1 = new Kind(Type.SPENDING, "Продукты");
        kind1 = kindRepository.save(kind1);

        Kind kind2 = new Kind(Type.SPENDING, "Транспорт");
        kind2 = kindRepository.save(kind2);

        Kind kind3 = new Kind(Type.SPENDING, "Комуналка");
        kind3 = kindRepository.save(kind3);

        Kind kind4 = new Kind(Type.SPENDING, "Авто заправка");
        kind4 = kindRepository.save(kind4);

        Kind kind5 = new Kind(Type.SPENDING, "Авто ремонт");
        kind5 = kindRepository.save(kind5);


        User user1 = new User("Vladimir", "mail@gmail.com", "123", Role.ROLE_ADMIN);
        user1 = userRepository.saveUser(prepareToSave(user1, passwordEncoder));

        User user2 = new User("Yana", "mail@ya.ru", "123", Role.ROLE_USER);
        user2.setGroup(user1.getGroup());
        user2 = userRepository.saveUser(prepareToSave(user2, passwordEncoder));

        User user3 = new User("Test", "test@gmail.com", "123", Role.ROLE_USER);
        user3 = userRepository.saveUser(prepareToSave(user3, passwordEncoder));


        /*Budget budget = new Budget(user1, kind, LocalDateTime.now(), "ZP", 1000.0);
        Budget budget1 = new Budget(user1, kind1, LocalDateTime.now(), "broad", 9.0);
        Budget budget2 = new Budget(user1, kind1, LocalDateTime.now(),"buter", 10.0);
        Budget budget3 = new Budget(user2, kind4, LocalDateTime.now(),"Fuel", 500.0);
        Budget budget4 = new Budget(user3, kind4, LocalDateTime.now(),"Diesel", 220.0);
        */

        /* User user1 = userRepository.findByNameIgnoreCase("Vladimir");
        Kind kind = kindRepository.findByNameIgnoreCase("Зарплата");
        Kind kind1 = kindRepository.findByNameIgnoreCase("Продукты");

        Budget budget = new Budget(user1, kind, LocalDateTime.now(), "ZP", 1000.0);
        Budget budget1 = new Budget(user1, kind1, LocalDateTime.now(), "broad", 9.0);
        Budget budget2 = new Budget(user1, kind1, LocalDateTime.now(),"buter", 10.0);*/


        /*budgetRepository.saveBudget(budget);
        budgetRepository.saveBudget(budget1);
        budgetRepository.saveBudget(budget2);
        budgetRepository.saveBudget(budget3);
        budgetRepository.saveBudget(budget4);


        for (Budget b : budgetRepository.findAll()) {
            System.out.println(b);
        }*/
    }
}

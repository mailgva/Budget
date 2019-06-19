package com.gorbatenko.budget;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Role;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;

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



    //@PostConstruct
    public void init() {
        /*
        budgetRepository.deleteAll();
        kindRepository.deleteAll();
        userRepository.deleteAll();

        User user1 = new User("Vladimir", "mail@gmail.com", "{noop}123", Role.ROLE_ADMIN);
        user1 = userRepository.saveUser((user1));

        User user2 = new User("Yana", "mail@ya.ru", "{noop}123", Role.ROLE_USER);
        user2.setGroup(user1.getGroup());
        user2 = userRepository.saveUser((user2));

        User user3 = new User("Test", "test@gmail.com", "{noop}123", Role.ROLE_USER);
        user3 = userRepository.saveUser((user3));

        Kind kind = new Kind(Type.PROFIT, "Зарплата", user1.getGroup());
        kind = kindRepository.save(kind);

        Kind kind0 = new Kind(Type.PROFIT, "Доп. доход", user1.getGroup());
        kind0 = kindRepository.save(kind0);

        Kind kind1 = new Kind(Type.SPENDING, "Продукты", user1.getGroup());
        kind1 = kindRepository.save(kind1);

        Kind kind2 = new Kind(Type.SPENDING, "Транспорт", user1.getGroup());
        kind2 = kindRepository.save(kind2);

        Kind kind3 = new Kind(Type.SPENDING, "Комуналка", user1.getGroup());
        kind3 = kindRepository.save(kind3);

        Kind kind4 = new Kind(Type.SPENDING, "Авто заправка", user1.getGroup());
        kind4 = kindRepository.save(kind4);

        Kind kind5 = new Kind(Type.SPENDING, "Авто ремонт", user1.getGroup());
        kind5 = kindRepository.save(kind5);

        */



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

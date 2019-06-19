package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Type;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends MongoRepository<Budget, String > {

    default Budget saveBudget(Budget budget) {
        if(budget.getUserGroup() == null) {
            budget.setUserGroup(budget.getUser().getGroup());
        }

        if(budget.getId() == null) {
            budget.setCreateDateTime(LocalDateTime.now());
        }
        return save(budget);
    };

    List<Budget> getBudgetByKindTypeOrderByCreateDateTime(Type type);


    List<Budget> getBudgetByKindTypeAndUser_GroupOrderByCreateDateTime(Type type, String userGroup);

    List<Budget> getBudgetByUser_Group(String userGroup);

}

package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BudgetRepository extends MongoRepository<Budget, String > {

    default Budget saveBudget(Budget budget) {
        if(budget.getId() == null) {
            budget.setCreateDateTime(LocalDateTime.now());
        }
        return save(budget);
    };

    default List<Budget> getBudgetByType(String type) {
        return getBudgetByTypeOrderByCreateDateTimeAsc(type);
    };

    List<Budget> getBudgetByTypeOrderByCreateDateTimeAsc(String type);

    List<Budget> getBudgetByUserGroup(String group);
}

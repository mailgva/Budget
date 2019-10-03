package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

    List<Budget> getBudgetByKindTypeOrderByCreateDateTimeDesc(Type type);


    List<Budget> getBudgetByKindTypeAndUser_GroupOrderByDateDesc(Type type, String userGroup);

    List<Budget> getBudgetByDateAndUser_Group(LocalDateTime date, String userGroup);

    List<Budget> getBudgetByDateBetweenAndUser_Group(LocalDateTime startDate, LocalDateTime endDate, String userGroup);

    List<Budget> getBudgetByKindAndDateBetweenAndUser_Group(Kind kind, LocalDateTime startDate, LocalDateTime endDate, String userGroup);

    List<Budget> getBudgetBykindAndUser_Group(Kind kind, String userGroup);

    List<Budget> findAllByDate(LocalDateTime date);

    List<Budget> getBudgetByUser_GroupOrderByDateDesc(String userGroup);

    int countByUser_GroupAndKind(String userGroup, Kind kind);



}

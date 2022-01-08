package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BudgetRepository extends MongoRepository<Budget, String > {

    default Budget saveBudget(Budget budget, User user) {
        if(budget.getUserGroup() == null) {
            budget.setUserGroup(user.getGroup());
        }

        if(budget.getId() == null) {
            budget.setCreateDateTime(LocalDateTime.now());
        }
        return save(budget);
    };

    List<Budget> getBudgetByDateAndUserGroup(LocalDateTime date, String userGroup);

    List<Budget> getBudgetByDateBetweenAndUserGroup(LocalDateTime startDate, LocalDateTime endDate, String userGroup);

    List<Budget> getBudgetByKindAndDateBetweenAndUserGroup(Kind kind, LocalDateTime startDate, LocalDateTime endDate, String userGroup);

    List<Budget> getBudgetByKindAndUserGroup(Kind kind, String userGroup);

    List<Budget> getBudgetByCurrencyAndUserGroup(Currency currency, String userGroup);

    List<Budget> getBudgetByuserGroupOrderByDateDesc(String userGroup);

    int countByUserGroupAndKind(String userGroup, Kind kind);

    int countByUserGroupAndCurrency(String userGroup, Currency currency);

    List<Budget> getBudgetByUserGroupAndDateLessThan(String userGroup, LocalDateTime date);

    List<Budget> getBudgetByUserGroupAndDateLessThanEqual(String userGroup, LocalDateTime date);

    List<Budget> getAllByUserGroup(String userGroup);

    List<Budget> getAllByKindTypeAndUserGroup(Type type, String userGroup);

    List<Budget> getAllByKindTypeAndDateBetweenAndUserGroup(Type type, LocalDateTime startDate, LocalDateTime endDate, String userGroup);

    List<Budget> getAllByUserId(String id);

}

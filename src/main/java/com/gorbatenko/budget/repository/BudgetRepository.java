package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BudgetRepository extends MongoRepository<Budget, String > {

    List<Budget> getBudgetByType(String type);

}

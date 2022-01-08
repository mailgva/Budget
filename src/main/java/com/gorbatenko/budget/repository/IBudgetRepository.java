package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBudgetRepository extends MongoRepository<Budget, String > {
}

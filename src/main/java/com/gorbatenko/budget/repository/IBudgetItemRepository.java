package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.BudgetItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBudgetItemRepository extends MongoRepository<BudgetItem, String > {
}

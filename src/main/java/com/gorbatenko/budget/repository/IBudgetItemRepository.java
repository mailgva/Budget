package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.BudgetItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public interface IBudgetItemRepository extends MongoRepository<BudgetItem, String > {
}

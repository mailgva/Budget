package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICurrencyRepository extends MongoRepository<Currency, String > {
}

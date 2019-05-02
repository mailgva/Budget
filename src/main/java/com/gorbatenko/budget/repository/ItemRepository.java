package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Item, String > {
}

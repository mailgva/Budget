package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository  extends MongoRepository<Currency, String > {

  Currency findByNameIgnoreCase(String name);

  Currency findByUserGroupAndId(String userGroup, String id);

  Currency findByUserGroupAndNameIgnoreCase(String userGroup, String name);

  List<Currency> findByUserGroup(String userGroup);

  List<Currency> findByUserGroupOrderByNameAsc(String userGroup);
}

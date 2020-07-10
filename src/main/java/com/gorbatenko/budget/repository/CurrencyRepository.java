package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository  extends MongoRepository<Currency, String > {

  Currency getCurrencyByUserGroupAndId(String userGroup, String id);

  Currency getCurrencyByUserGroupAndNameIgnoreCase(String userGroup, String name);

  List<Currency> getCurrencyByUserGroup(String userGroup);

  List<Currency> getCurrencyByUserGroupOrderByNameAsc(String userGroup);
}

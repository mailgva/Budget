package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.RegularOperation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegularOperationRepository  extends MongoRepository<RegularOperation, String > {
    List<RegularOperation> getByUserGroupOrderByEveryPositAsc(String userGroup);

    List<RegularOperation> getByKindAndUserGroup(Kind kind, String userGroup);

    List<RegularOperation> getByCurrencyAndUserGroup(Currency currency, String userGroup);

    RegularOperation getRegularOperationByUserGroupAndId(String userGroup, String id);

    int countByUserGroupAndKind(String userGroup, Kind kind);

    int countByUserGroupAndCurrency(String userGroup, Currency currency);
}

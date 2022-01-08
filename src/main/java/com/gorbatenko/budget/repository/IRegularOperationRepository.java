package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.RegularOperation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRegularOperationRepository extends MongoRepository<RegularOperation, String > {
}

package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Kind;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IKindRepository extends MongoRepository<Kind, String> {
}

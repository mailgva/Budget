package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Kind;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public interface IKindRepository extends MongoRepository<Kind, String> {
}

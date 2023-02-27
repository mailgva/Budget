package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.JoinRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public interface IJoinRequestRepository extends MongoRepository<JoinRequest, String> {
}


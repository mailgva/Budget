package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.JoinRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IJoinRequestRepository extends MongoRepository<JoinRequest, String> {
}


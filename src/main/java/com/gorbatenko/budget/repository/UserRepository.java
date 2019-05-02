package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}

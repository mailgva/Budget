package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.UserGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserGroupRepository extends MongoRepository<UserGroup, String> {
}

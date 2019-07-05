package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface KindRepository extends MongoRepository<Kind, String > {

    Kind findByNameIgnoreCase(String name);

    Kind findKindByUserGroupAndId(String userGroup, String id);

    List<Kind> findByType(Type type);

    List<Kind> findByTypeAndUserGroup(Type type, String userGroup);

    List<Kind> findByUserGroup(String userGroup);

}

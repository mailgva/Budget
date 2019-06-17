package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface KindRepository extends MongoRepository<Kind, String > {

    Kind findByNameIgnoreCase(String name);

    List<Kind> findByType(Type type);

    //List<Kind> find();

}

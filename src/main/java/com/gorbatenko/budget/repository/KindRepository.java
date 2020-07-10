package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KindRepository extends MongoRepository<Kind, String> {

  Kind getKindByUserGroupAndId(String userGroup, String id);

  Kind getKindByUserGroupAndTypeAndNameIgnoreCase(String userGroup, Type type, String name);

  List<Kind> getKindByTypeAndUserGroup(Type type, String userGroup);

  List<Kind> getKindByUserGroupOrderByTypeAscNameAsc(String userGroup);

}

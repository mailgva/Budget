package com.gorbatenko.budget.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;
import static java.util.Objects.requireNonNull;

@Repository
public class AbstractRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    private Criteria addCriteriaUserGroup(Criteria criteria) {
        String userGroup = getUserGroup();
        if (criteria == null) {
            criteria = new Criteria();
        }
        criteria.and("userGroup").is(userGroup);
        return criteria;
    }

    protected <T> List<T> findAll(Criteria criteria, Sort sort, Class<T> clazz) {
        Query query = new Query(addCriteriaUserGroup(criteria));
        if (sort != null) {
            query.with(sort);
        }
        return mongoTemplate.find(query, clazz);
    }

    protected <T> T findOne(Criteria criteria, Class<T> clazz) {
        Query query = new Query(addCriteriaUserGroup(criteria));
        return mongoTemplate.findOne(query, clazz);
    }

    protected <T> T findById(String id, Class<T> clazz) {
        requireNonNull(id, "Argument 'Id' can not be null!");
        Criteria criteria = new Criteria();
        criteria.and("id").is(id);
        return findOne(criteria, clazz);
    }

    protected <E,R> List<R> findDistinctSubclassInCollection(Criteria criteria, String field, Class<E> entityClass, Class<R> resultClass) {
        Query query = new Query(addCriteriaUserGroup(criteria));
        return mongoTemplate.findDistinct(query, field, entityClass, resultClass);
    }

    protected boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}

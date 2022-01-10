package com.gorbatenko.budget.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

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

    public <E, R> R aggregationOnlyResultField(Criteria criteria, GroupOps groupOps, String field, Class<E> entityClass, Class<R> resultClass) {
        ProjectionOperation excludeIdField = project().andExclude("_id");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(addCriteriaUserGroup(criteria)),
                addGroupOperation(groupOps, field),
                excludeIdField
        );
        try {
            return mongoTemplate.aggregate(aggregation, entityClass, resultClass).getUniqueMappedResult();
        } catch (Exception e) {
            return (R) mongoTemplate.aggregate(aggregation, entityClass, Map.class).getUniqueMappedResult().get(field);
        }
    }

    protected boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public enum GroupOps {
        SUM, MIN, MAX, AVG
    }

    private GroupOperation addGroupOperation(GroupOps groupOps, String field) {
        GroupOperation result = Aggregation.group();
        switch (groupOps) {
            case AVG: return result.avg(field).as(field);
            case MIN: return result.min(field).as(field);
            case MAX: return result.max(field).as(field);
            case SUM: return result.sum(field).as(field);
            default: throw new IllegalArgumentException("GroupOps does not exist");
        }
    }
}

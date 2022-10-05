package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.util.KindTotals;
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
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class AbstractRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    private Criteria addCriteriaUserGroup(Criteria criteria) {
        String userGroup = getUserGroup();
        Criteria userGroupCriteria = Criteria.where("userGroup").is(userGroup);
        if (criteria == null) {
            return new Criteria().andOperator(userGroupCriteria);
        }
        return new Criteria().andOperator(userGroupCriteria, criteria);
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

    public <E, R> R aggregationOnlyResultField(Criteria criteria, GroupOps groupOps, String field, Class<E> entityClass, Class<R> resultClass, Object defaultValue) {
        ProjectionOperation excludeIdField = project().andExclude("_id");
        Aggregation aggregation = Aggregation.newAggregation(
                match(addCriteriaUserGroup(criteria)),
                addGroupOperation(groupOps, null, field, field),
                excludeIdField
        );
        try {
            R result = mongoTemplate.aggregate(aggregation, entityClass, resultClass).getUniqueMappedResult();
            return (result == null ? (R) defaultValue : result);
        } catch (Exception e) {
            return (R) mongoTemplate.aggregate(aggregation, entityClass, Map.class).getUniqueMappedResult().getOrDefault(field, defaultValue);
        }
    }

    public <E, R> List<R> aggregationByField(Criteria criteria, GroupOps groupOps, String groupByField, String field, String asField, Class<E> entityClass, Class<R> resultClass) {
        ProjectionOperation projection = project(asField).and(groupByField).previousOperation();
        Aggregation aggregation = Aggregation.newAggregation(
                match(addCriteriaUserGroup(criteria)),
                addGroupOperation(groupOps, groupByField, field, asField),
                projection
        );
        return mongoTemplate.aggregate(aggregation, entityClass, resultClass).getMappedResults();
    }

    public List<KindTotals> getTotalsByKinds(Criteria criteria) {
        ProjectionOperation projection = project("sumPrice", "count", "minCreateDateTime", "maxCreateDateTime")
                .and("kind").previousOperation();
        Aggregation aggregation = Aggregation.newAggregation(
                match(addCriteriaUserGroup(criteria)),
                group("kind")
                        .sum("price").as("sumPrice")
                        .count().as("count")
                        .min("createDateTime").as("minCreateDateTime")
                        .max("createDateTime").as("maxCreateDateTime"),
                projection
        );
        return mongoTemplate.aggregate(aggregation, BudgetItem.class, KindTotals.class).getMappedResults();
    }

    protected boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public enum GroupOps {
        SUM, MIN, MAX, AVG, COUNT
    }

    private GroupOperation addGroupOperation(GroupOps groupOps, String groupByField, String field, String asField) {
        GroupOperation result = groupByField == null ? Aggregation.group() : Aggregation.group(groupByField);
        switch (groupOps) {
            case AVG: return result.avg(field).as(asField == null ? field : asField);
            case MIN: return result.min(field).as(asField == null ? field : asField);
            case MAX: return result.max(field).as(asField == null ? field : asField);
            case SUM: return result.sum(field).as(asField == null ? field : asField);
            case COUNT: return result.count().as(asField == null ? field : asField);
            default: throw new IllegalArgumentException("GroupOps does not exist");
        }
    }
}

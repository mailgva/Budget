package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
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
public class CurrencyRepository extends AbstractRepository {
    private MongoTemplate mongoRepository;

    private ICurrencyRepository repository;

    @Autowired
    public void setMongoRepository(MongoTemplate mongoRepository) {
        this.mongoRepository = mongoRepository;
    }
    @Autowired
    public void setRepository(ICurrencyRepository repository) {
        this.repository = repository;
    }

    public Currency save(Currency currency) {
        if (currency.getUserGroup() == null) {
            currency.setUserGroup(getUserGroup());
        }
        return repository.save(currency);
    }

    public void deleteById(String id) {
        repository.delete(this.getById(id));
    }

    public List<Currency> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return findAll(null, sort, Currency.class);
    }

    public List<Currency> getVisibled() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Criteria criteria = new Criteria();
        criteria.orOperator(new Criteria().and("hidden").is(false), new Criteria().and("hidden").is(null));
        return findAll(criteria, sort, Currency.class);
    }

    public Currency getById(String id) {
        return findById(id, Currency.class);
    }

    public Currency getByName(String name) {
        requireNonNull(name, "Argument 'name' can not be null!");
        Criteria criteria = new Criteria();
        criteria.and("name").is(name);
        return findOne(criteria, Currency.class);
    }

    public Currency getByUserGroupAndName(String userGroup, String name) {
        Criteria criteria = new Criteria();
        criteria.and("userGroup").is(userGroup);
        criteria.and("name").is(name);
        Query query = new Query(criteria);
        return mongoRepository.findOne(query, Currency.class);
    }

    public List<Currency> getFilteredData(String id, String name, boolean hidden) {
        Criteria criteria = new Criteria();

        if (!isBlank(id)) {
            criteria.and("id").is(id);
        }
        if (!isBlank(name)) {
            criteria.and("name").is(name);
        }
        if (hidden) {
            criteria.and("hidden").is(hidden);
        } else {
            criteria.orOperator(new Criteria().and("hidden").is(false), new Criteria().and("hidden").is(null));
        }
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return findAll(criteria, sort, Currency.class);
    }
}

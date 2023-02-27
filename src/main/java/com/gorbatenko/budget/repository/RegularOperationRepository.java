package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.RegularOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;
import static java.util.Objects.requireNonNull;

@Repository
public class RegularOperationRepository extends AbstractRepository {
    private MongoTemplate mongoRepository;

    private IRegularOperationRepository repository;

    @Autowired
    public void setMongoRepository(MongoTemplate mongoRepository) {
        this.mongoRepository = mongoRepository;
    }
    @Autowired
    public void setRepository(IRegularOperationRepository repository) {
        this.repository = repository;
    }

    public RegularOperation save(RegularOperation regularOperation) {
        if (regularOperation.getUserGroup() == null) {
            regularOperation.setUserGroup(getUserGroup());
        }
        return repository.save(regularOperation);
    }

    public void deleteById(String id) {
        repository.delete(this.getById(id));
    }

    public List<RegularOperation> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "every.posit");
        return findAll(null, sort, RegularOperation.class);
    }

    public List<RegularOperation> adminGetAll() {
        return mongoRepository.findAll(RegularOperation.class);
    }

    public RegularOperation getById(String id) {
        return findById(id, RegularOperation.class);
    }

    public List<RegularOperation> getFilteredData(String id, Kind kind, String description, Currency currency, Double price) {
        Criteria criteria = new Criteria();

        if (!isBlank(id)) {
            criteria.and("id").is(id);
        }
        if (kind != null) {
            criteria.and("kind._id").is(kind.getId());
        }
        if (!isBlank(description)) {
            criteria.and("description").is(description);
        }
        if (currency != null) {
            criteria.and("currency._id").is(currency.getId());
        }
        if (price != null) {
            criteria.and("price").is(price);
        }
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return findAll(criteria, sort, RegularOperation.class);
    }

    public List<RegularOperation> getByCurrencyId(String currencyId) {
        requireNonNull(currencyId, "Argument 'currencyId' can not be null!");
        Criteria criteria = new Criteria();
        criteria.and("currency._id").is(currencyId);
        return findAll(criteria, null, RegularOperation.class);
    }

    public List<RegularOperation> getByKindId(String kindId) {
        requireNonNull(kindId, "Argument 'kindId' can not be null!");
        Criteria criteria = new Criteria();
        criteria.and("kind._id").is(kindId);
        return findAll(criteria, null, RegularOperation.class);
    }
}

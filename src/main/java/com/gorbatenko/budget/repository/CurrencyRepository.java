package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;
import static java.util.Objects.requireNonNull;

@Repository
public class CurrencyRepository extends AbstractRepository {
    @Autowired
    private ICurrencyRepository repository;

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

    public Currency getById(String id) {
        return findById(id, Currency.class);
    }

    public Currency getByName(String name) {
        requireNonNull(name, "Argument 'name' can not be null!");
        Criteria criteria = new Criteria();
        criteria.and("name").is(name);
        return findOne(criteria, Currency.class);
    }

    public List<Currency> getFilteredData(String id, String name) {
        Criteria criteria = new Criteria();

        if (!isBlank(id)) {
            criteria.and("id").is(id);
        }
        if (!isBlank(name)) {
            criteria.and("name").is(name);
        }

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return findAll(criteria, sort, Currency.class);
    }
}

package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.doc.User;
import com.gorbatenko.budget.util.TypePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;
import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;
import static java.util.Objects.requireNonNull;

@Repository
public class BudgetRepository extends AbstractRepository {
    @Autowired
    private IBudgetRepository repository;

    public Budget save(Budget budget) {
        if (budget.getUserGroup() == null) {
            budget.setUserGroup(getUserGroup());
        }
        return repository.save(budget);
    }

    public Budget adminSave(Budget budget) {
        return repository.save(budget);
    }

    public void deleteById(String id) {
        repository.delete(this.getById(id));
    }

    public List<Budget> getAll() {
        Criteria criteria = new Criteria();
        criteria.and("currency._id").is(getCurrencyDefault().getId());
        Sort sort = Sort.by(Sort.Direction.DESC, "date");
        return findAll(criteria, sort, Budget.class);
    }

    public List<Budget> getFilteredData(LocalDateTime startDate, LocalDateTime endDate, String userId, String typeStr, String kindId, String priceStr, String description, TypePeriod period) {
        Criteria criteria = new Criteria();
        criteria.and("currency._id").is(getCurrencyDefault().getId());

        if (!TypePeriod.ALL_TIME.equals(period)) {
            if (startDate != null && endDate != null) {
                criteria.and("date").gte(startDate).lte(endDate);
            } else {
                if (startDate != null) {
                    criteria.and("date").gte(startDate);
                }
                if (endDate != null) {
                    criteria.and("date").lte(endDate);
                }
            }
        }
        if (!isBlank(userId) && !isFakeValue(userId)) {
            criteria.and("user._id").is(userId);
        }
        if (!isBlank(typeStr) && !isFakeValue(typeStr)) {
            criteria.and("kind.type").is(Type.valueOf(typeStr));
        }
        if (!isBlank(kindId) && !isFakeValue(kindId)) {
            criteria.and("kind._id").is(kindId);
        }
        if (!isBlank(description)) {
            criteria.and("description").regex(Pattern.compile(description, Pattern.CASE_INSENSITIVE));
        }
        if (!isBlank(priceStr)) {
            String[] prices = priceStr.trim().split("\\p{P}");

            if (prices.length == 1) {
                criteria.and("price").is(Double.valueOf(prices[0]));
            } else {
                criteria.and("price").gte(Double.valueOf(prices[0])).lte(Double.valueOf(prices[1]));
            }
        }
        Sort sort = Sort.by(Sort.Direction.ASC, "date");
        return findAll(criteria, sort, Budget.class);
    }

    public Budget getById(String id) {
        return findById(id, Budget.class);
    }

    public List<Budget> getByCurrencyId(String currencyId) {
        requireNonNull(currencyId, "Argument 'currencyId' can not be null!");
        Criteria criteria = new Criteria();
        criteria.and("currency._id").is(currencyId);
        return findAll(criteria, null, Budget.class);
    }

    public List<Budget> getByKindId(String kindId) {
        requireNonNull(kindId, "Argument 'kindId' can not be null!");
        Criteria criteria = new Criteria();
        criteria.and("kind._id").is(kindId);
        return findAll(criteria, null, Budget.class);
    }

    public LocalDateTime getMaxDate() {
        return aggregationOnlyResultField(null, GroupOps.MAX, "date", Budget.class, LocalDateTime.class);
    }

    public Double getSumPriceByType(Type type) {
        Criteria criteria = new Criteria();
        criteria.and("kind.type").is(type);
        return aggregationOnlyResultField(criteria, GroupOps.SUM, "price", Budget.class, Double.class);
    }

    public List<User> getUsersForAllPeriod() {
        return findDistinctSubclassInCollection(null, "user", Budget.class, User.class);
    }

    private boolean isFakeValue(String str) {
        List<String> fakes = List.of("-1", "ALLTYPES");
        return fakes.contains(str.toUpperCase());
    }

}

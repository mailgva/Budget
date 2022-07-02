package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.doc.User;
import com.gorbatenko.budget.util.BaseUtil;
import com.gorbatenko.budget.util.KindTotals;
import com.gorbatenko.budget.util.TypePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.BaseUtil.getStrYearMonth;
import static com.gorbatenko.budget.util.BaseUtil.getStrYearMonthDay;
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
        Criteria criteria = createBaseFilterCriteria(startDate, endDate, userId, typeStr, kindId, priceStr, description, period);
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

    public LocalDateTime getMinDateByCurrencyDefault() {
        Criteria criteria = new Criteria();
        criteria.and("currency._id").is(getCurrencyDefault().getId());
        return aggregationOnlyResultField(criteria, GroupOps.MIN, "date", Budget.class, LocalDateTime.class, LocalDateTime.MIN);
    }

    public LocalDateTime getMaxDateByCurrencyDefault() {
        Criteria criteria = new Criteria();
        criteria.and("currency._id").is(getCurrencyDefault().getId());
        return aggregationOnlyResultField(criteria, GroupOps.MAX, "date", Budget.class, LocalDateTime.class, LocalDateTime.MIN);
    }

    public LocalDateTime getMaxDate() {
        return aggregationOnlyResultField(null, GroupOps.MAX, "date", Budget.class, LocalDateTime.class, LocalDateTime.MIN);
    }

    public Double getSumPriceByDefaultCurrencyAndType(Type type) {
        return getSumPriceByCurrencyAndType(getCurrencyDefault(), type);
    }

    public Double getSumPriceByCurrencyAndType(Currency currency, Type type) {
        Criteria criteria = new Criteria();
        criteria.and("currency._id").is(currency.getId());
        criteria.and("kind.type").is(type);
        return aggregationOnlyResultField(criteria, GroupOps.SUM, "price", Budget.class, Double.class, 0.0D);
    }

    public Map<String, Double> getSumPriceForPeriodByDateAndDefaultCurrency(LocalDateTime startDate, LocalDateTime endDate,
                                                                     Type type, TypePeriod period, boolean isInMonth) {
        Criteria criteria = createBaseFilterCriteria(startDate, endDate, null, type.name(), null, null, null, period);

        List<DateSumPrice> dateSumPrices = aggregationByField(criteria, GroupOps.SUM, "date", "price", "sumPrice", Budget.class, DateSumPrice.class);

        Map<String, Double> result = new HashMap<>();
        for(DateSumPrice item : dateSumPrices) {
            String key = isInMonth ? getStrYearMonthDay(item.getDate()) :  getStrYearMonth(item.getDate());
            Double value = result.getOrDefault(key, 0.0D) + item.sumPrice;
            result.put(key, value);
        }
        return result;
    }

    public List<User> getUsersForAllPeriod() {
        Criteria criteria = new Criteria();
        criteria.and("currency._id").is(getCurrencyDefault().getId());
        return findDistinctSubclassInCollection(criteria, "user", Budget.class, User.class);
    }

    public List<KindTotals> getTotalsByKinds(LocalDateTime startDate, LocalDateTime endDate, String userId, String typeStr, String kindId, String priceStr, String description, TypePeriod period){
        Criteria criteria = createBaseFilterCriteria(startDate, endDate, userId, typeStr, kindId, priceStr, description, period);
        return getTotalsByKinds(criteria);
    }

    private Criteria createBaseFilterCriteria(LocalDateTime startDate, LocalDateTime endDate, String userId, String typeStr, String kindId, String priceStr, String description, TypePeriod period) {
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
        return criteria;
    }

    private boolean isFakeValue(String str) {
        List<String> fakes = List.of("-1", "ALLTYPES");
        return fakes.contains(str.toUpperCase());
    }

    class DateSumPrice {
        private LocalDateTime date;
        private Double sumPrice;

        public DateSumPrice(LocalDateTime date, Double sumPrice) {
            this.date = date;
            this.sumPrice = sumPrice;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public Double getSumPrice() {
            return sumPrice;
        }
    }
}

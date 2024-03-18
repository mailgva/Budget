package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;
import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;

@Repository
@RequiredArgsConstructor
public class BudgetItemRepository {

    private final IBudgetItemRepository repository;
    private final NamedParameterJdbcTemplate npjTemplate;

    public BudgetItem save(BudgetItem budgetItem) {
        budgetItem.setUserGroup(getUserGroup());
        if (!StringUtils.hasText(budgetItem.getDescription())) {
            budgetItem.setDescription(null);
        }
        return repository.save(budgetItem);
    }

    @Transactional
    public void saveAll(List<BudgetItem> budgetItems) {
        for(BudgetItem budgetItem : budgetItems) {
            budgetItem.setUserGroup(getUserGroup());
            if (!StringUtils.hasText(budgetItem.getDescription())) {
                budgetItem.setDescription(null);
            }
        }
        repository.saveAll(budgetItems);
    }

    public BudgetItem adminSave(BudgetItem budgetItem) {
        return repository.save(budgetItem);
    }

    public void deleteById(UUID id) {
        repository.deleteByUserGroupAndId(getUserGroup(), id);
    }

    public List<BudgetItem> findAll() {
        return repository.findAllByUserGroupAndCurrencyId(getUserGroup(), getCurrencyDefault().getId());
    }

    public BudgetItem findById(UUID id) {
        return repository.findByUserGroupAndId(getUserGroup(), id);
    }

    public List<BudgetItem> findByCurrencyId(UUID currencyId) {
        return repository.findAllByUserGroupAndCurrencyId(getUserGroup(), currencyId);
    }

    public List<BudgetItem> findByKindId(UUID kindId) {
        return repository.findAllByUserGroupAndKindId(getUserGroup(), kindId);
    }

    public LocalDateTime getMinDateByCurrencyDefault() {
        return repository.findMinDateByUserGroupAndCurrencyId(getUserGroup(),getCurrencyDefault().getId());
    }

    public LocalDateTime getMaxDateByCurrencyDefault() {
        return repository.findMaxDateByUserGroupAndCurrencyId(getUserGroup(), getCurrencyDefault().getId());
    }

    public LocalDate getMaxDate() {
        LocalDate maxDateByUserGroup = repository.findMaxDateByUserGroup(getUserGroup());
        return  maxDateByUserGroup == null ? LocalDate.MIN : maxDateByUserGroup;
    }

    public UUID findLastCurrencyId(LocalDate date) {
        return repository.findLastCurrencyIdByUserGroup(getUserGroup(), date);
    }

    public Double getSumPriceByDefaultCurrencyAndType(Type type) {
        return getSumPriceByCurrencyAndType(getCurrencyDefault(), type);
    }

    public Double getSumPriceByCurrencyAndType(Currency currency, Type type) {
        Double value = repository.findSumPriceByUserGroupAndCurrencyIdAndType(getUserGroup(), currency.getId(), type.name());
        return value == null ? 0.0D : value;
    }

    public Map<String, Double> getSumPriceForPeriodByDateAndDefaultCurrency(LocalDate startDate, LocalDate endDate,
                                                                     Type type, TypePeriod period, GroupPeriod groupPeriod) {
        List<IDateSumPrice> dateSumPricesAll = TypePeriod.ALL_TIME.equals(period) ?
                repository.findDateSumPriceByUserGroupAndCurrencyIdAndType(
                        getUserGroup(), getCurrencyDefault().getId(), type.name()) :
                repository.findDateSumPriceForPeriodByUserGroupAndCurrencyIdAndType(
                        getUserGroup(), getCurrencyDefault().getId(), type.name(), startDate, endDate);

        List<DateSumPrice> dateSumPrices = dateSumPricesAll.stream()
                .map(item -> new DateSumPrice(item.getDate(), item.getSumPrice()))
                .toList();

        Map<String, Double> result = new HashMap<>();
        for(DateSumPrice item : dateSumPrices) {
            String key = groupPeriod.formatDate(item.getDate());
            Double value = result.getOrDefault(key, 0.0D) + item.getSumPrice();
            result.put(key, value);
        }
        return result;
    }

    public List<User> getUsersForAllPeriod() {
        String sql = """
            select distinct u.id, u.name
            from budget_items bi
            join users u on bi.user_id = u.id
            where bi.user_group = :userGroup and bi.currency_id = :currencyId
            order by u.name""";

        MapSqlParameterSource params = new MapSqlParameterSource("userGroup", getUserGroup())
                .addValue("currencyId", getCurrencyDefault().getId());

        return npjTemplate.query(sql, params, new BeanPropertyRowMapper<>(User.class));
    }

    public TreeMap<Currency, Long> getCurrencyCounts() {
        TreeMap<Currency, Long> result = new TreeMap<>();
        String sql = """
              select c.id, c.user_group userGroup, c.name, c.hidden, count(bi.currency_id) cnt
              from currencies c
              left join budget_items bi on c.id = bi.currency_id
              where c.user_group = :userGroup
              group by c.id, c.user_group, c."name", c.hidden
              order by c.name""";

        MapSqlParameterSource params = new MapSqlParameterSource("userGroup", getUserGroup());

        npjTemplate.query(sql, params, rs -> {
            Currency currency = new Currency();
            currency.setId(UUID.fromString(rs.getString("id")));
            currency.setUserGroup(UUID.fromString(rs.getString("userGroup")));
            currency.setName(rs.getString("name"));
            currency.setHidden(rs.getBoolean("hidden"));
            result.put(currency, rs.getLong("cnt"));
        });

        return result;
    }

    public List<KindTotals> getTotalsByKindsForPeriod(LocalDate startDate, LocalDate endDate, TypePeriod period){
        String sql = """
                select k.id, k.name, k.type, sum(bi.price) sumPrice, count(*) "count",
                  min(bi.date_at) minCreateDate,  max(bi.date_at) maxCreateDate
                from budget_items bi
                join kinds k on bi.kind_id = k.id
                where bi.user_group = :userGroup and bi.currency_id = :currencyId\n""";
        sql = sql + (TypePeriod.ALL_TIME.equals(period) ? "" :
                " and (bi.date_at between :startDate and :endDate)\n");
        sql = sql + "group by k.id, k.name, k.type";

        MapSqlParameterSource params = new MapSqlParameterSource("userGroup", getUserGroup())
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("currencyId", getCurrencyDefault().getId());

        return npjTemplate.query(sql, params, (rs, rowNum) -> {
            UUID id = UUID.fromString(rs.getString("id"));
            String name = rs.getString("name");
            String type = rs.getString("type");
            Kind kind = new Kind(id, name, Type.from(type));
            Double sumPrice = rs.getDouble("sumPrice");
            Long count = rs.getLong("count");
            LocalDate minCreateDate = rs.getDate("minCreateDate").toLocalDate();
            LocalDate maxCreateDate = rs.getDate("maxCreateDate").toLocalDate();
            return new KindTotals(kind, sumPrice, count, minCreateDate, maxCreateDate);
        });
    }

    public Double getRemainByDefaultCurrencyForDate(LocalDate date) {
        return repository.getRemainByDefaultCurrencyForDate(getUserGroup(), getCurrencyDefault().getId(), date);
    }

    public List<Kind> getPopularKindByTypeForPeriod(Type type, LocalDate startDate, LocalDate endDate, int popularCount) {
        String sql = """
                select k.id, k.user_group, k."name", k."type", k.hidden, count(*) cnt
                from budget_items bi
                join kinds k on bi.kind_id = k.id and k.type = :type
                where bi.user_group = :userGroup and bi.currency_id = :currencyId
                  and bi.date_at between :startDate and :endDate
                group by k.id, k.user_group, k."name", k."type", k.hidden
                having count(*) >= :popularCount
                order by 6 desc, 3""";

        MapSqlParameterSource params = new MapSqlParameterSource("userGroup", getUserGroup())
                .addValue("currencyId", getCurrencyDefault().getId())
                .addValue("type", type.name())
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("popularCount", popularCount);

        return npjTemplate.query(sql, params, new BeanPropertyRowMapper<>(Kind.class));
    }

    public List<BudgetItem> findBySelectedPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = """
                select bi.id, bi.user_group userGroup, bi.date_at dateAt, bi.created_at createdAt, bi.kind_id kindId,
                    bi.description, bi.price, bi.kind_id kindId, k.name kindName, k.type kindType, 
                    bi.user_id userId, u.name userName
                from budget_items bi
                join kinds k on bi.kind_id = k.id
                join users u on bi.user_id = u.id                
                where bi.user_group = :userGroup and bi.currency_id = :currencyId
                  and bi.date_at between :startDate and :endDate
                order by bi.date_at""";

        MapSqlParameterSource params = new MapSqlParameterSource("userGroup", getUserGroup())
                .addValue("currencyId", getCurrencyDefault().getId())
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        return npjTemplate.query(sql, params, (rs, rowNum) -> {
            BudgetItem budgetItem = new BeanPropertyRowMapper<>(BudgetItem.class).mapRow(rs, rowNum);
            Kind kind = new Kind(Type.from(rs.getString("kindType")), rs.getString("kindName"));
            budgetItem.setKind(kind);
            budgetItem.setUser(new User(rs.getString("userName")));
            return budgetItem;
        });
    }

    public List<BudgetItem> findByKindIdAndSelectedPeriod(UUID kindId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                select bi.*
                from budget_items bi
                where bi.user_group = :userGroup and bi.currency_id = :currencyId and bi.kind_id = :kindId
                  and bi.date_at between :startDate and :endDate
                order by bi.date_at""";

        MapSqlParameterSource params = new MapSqlParameterSource("userGroup", getUserGroup())
                .addValue("currencyId", getCurrencyDefault().getId())
                .addValue("kindId", kindId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        return npjTemplate.query(sql, params, new BeanPropertyRowMapper<>(BudgetItem.class));
    }

    public List<BudgetItem> findByTypeAndSelectedPeriod(Type type, LocalDate startDate, LocalDate endDate) {
        String sql = """
                select bi.id, bi.user_group userGroup, bi.date_at dateAt, bi.created_at createdAt, bi.kind_id kindId,
                    bi.description, bi.price, bi.kind_id kindId
                from budget_items bi
                join kinds k on bi.kind_id = k.id
                where bi.user_group = :userGroup and bi.currency_id = :currencyId and k.type = :type
                  and bi.date_at between :startDate and :endDate
                order by bi.date_at""";

        MapSqlParameterSource params = new MapSqlParameterSource("userGroup", getUserGroup())
                .addValue("currencyId", getCurrencyDefault().getId())
                .addValue("type", type.name())
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        return npjTemplate.query(sql, params, new BeanPropertyRowMapper<>(BudgetItem.class));
    }

    public List<BudgetItem> getFilteredData(LocalDate startDate, LocalDate endDate, UUID userId, Type type,
                                            UUID kindId, String priceStr, String description, TypePeriod period) {
        StringBuilder sql = new StringBuilder("""
                select bi.id, bi.user_group userGroup, bi.date_at dateAt, bi.created_at createdAt, bi.kind_id kindId,
                    bi.description, bi.price, bi.kind_id kindId, k.name kindName, k.type kindType, 
                    bi.user_id userId, u.name userName
                from budget_items bi
                join kinds k on bi.kind_id = k.id
                join users u on bi.user_id = u.id
                where bi.user_group = :userGroup and bi.currency_id = :currencyId\n""");

        MapSqlParameterSource params = new MapSqlParameterSource("userGroup", getUserGroup())
                .addValue("currencyId", getCurrencyDefault().getId());


        if (!TypePeriod.ALL_TIME.equals(period)) {
            sql.append("and bi.date_at between :startDate and :endDate\n");
            params.addValue("startDate", startDate)
                    .addValue("endDate", endDate);
        }

        if (userId != null) {
            sql.append("and bi.user_id = :userId\n");
            params.addValue("userId", userId);
        }

        if (type != null) {
            sql.append("and k.type = :type\n");
            params.addValue("type", type.name());
        }

        if (kindId != null) {
            sql.append("and bi.kind_id = :kindId\n");
            params.addValue("kindId", kindId);
        }

        if (description != null) {
            sql.append("and upper(coalesce(bi.description, '')) like :description\n");
            params.addValue("description", "%"+description.toUpperCase()+"%");
        }

        if (priceStr != null) {
            String[] prices = priceStr.trim().split("\\p{P}");

            if (prices.length == 1) {
                if (isNumeric(prices[0])) {
                    sql.append("and bi.price = :price\n");
                    params.addValue("price", Double.valueOf(prices[0]));
                }
            } else {
                if (isNumeric(prices[0]) && isNumeric(prices[1])) {
                    sql.append("and bi.price >= :price1 and bi.price <= :price2\n");
                    params.addValue("price1", Double.valueOf(prices[0]));
                    params.addValue("price2", Double.valueOf(prices[1]));
                }
            }
        }

        sql.append("order by bi.date_at asc, bi.created_at asc");

        return npjTemplate.query(sql.toString(), params, (rs, rowNum) -> {
            BudgetItem budgetItem = new BeanPropertyRowMapper<>(BudgetItem.class).mapRow(rs, rowNum);
            Kind kind = new Kind(Type.from(rs.getString("kindType")), rs.getString("kindName"));
            budgetItem.setKind(kind);
            budgetItem.setUser(new User(rs.getString("userName")));
            return budgetItem;
        });
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}

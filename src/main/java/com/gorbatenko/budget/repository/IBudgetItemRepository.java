package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.util.IDateSumPrice;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public interface IBudgetItemRepository extends JpaRepository<BudgetItem, UUID> {
    @Transactional
    @Modifying
    void deleteByUserGroupAndId(UUID userGroup, UUID id);
    BudgetItem findByUserGroupAndId(UUID userGroup, UUID id);
    List<BudgetItem> findAllByUserGroupAndCurrencyId(UUID userGroup, UUID currencyId);
    List<BudgetItem> findAllByUserGroupAndKindId(UUID userGroup, UUID kindId);
    @Query(value = "select min(bi.date_at) from budget_items bi " +
            "where bi.user_group = :userGroup and bi.currency_id = :currencyId", nativeQuery = true)
    LocalDateTime findMinDateByUserGroupAndCurrencyId(UUID userGroup, UUID currencyId);

    @Query(value = """
            select max(bi.date_at) from budget_items bi
            where bi.user_group = :userGroup and bi.currency_id = :currencyId""", nativeQuery = true)
    LocalDateTime findMaxDateByUserGroupAndCurrencyId(UUID userGroup, UUID currencyId);

    @Query(value = "select max(bi.date_at) from budget_items bi where bi.user_group = :userGroup", nativeQuery = true)
    LocalDate findMaxDateByUserGroup(UUID userGroup);

    @Query(value = """
            select bi.currency_id from budget_items bi
            where bi.user_group = :userGroup and bi.date_at = :date
            order by bi.created_at desc limit 1""", nativeQuery = true)
    UUID findLastCurrencyIdByUserGroup(UUID userGroup, LocalDate date);

    @Query(value = """
        select sum(bi.price)
        from budget_items bi
        join kinds k on bi.kind_id = k.id and k.type = :type
        where bi.user_group = :userGroup and bi.currency_id = :currencyId""", nativeQuery = true)
    Double findSumPriceByUserGroupAndCurrencyIdAndType(UUID userGroup, UUID currencyId, String type);

    @Query(value = """
        select bi.date_at date, sum(bi.price) sumPrice
        from budget_items bi
        join kinds k on bi.kind_id = k.id and k.type = :type
        where bi.user_group = :userGroup and bi.currency_id = :currencyId
        and (bi.date_at between :dateStart and :dateEnd)
        group by bi.date_at""", nativeQuery = true)
    List<IDateSumPrice> findDateSumPriceForPeriodByUserGroupAndCurrencyIdAndType(
            UUID userGroup, UUID currencyId, String type, LocalDate dateStart, LocalDate dateEnd);

    @Query(value = """
        select bi.date_at date, sum(bi.price) sumPrice
        from budget_items bi
        join kinds k on bi.kind_id = k.id and k.type = :type
        where bi.user_group = :userGroup and bi.currency_id = :currencyId
        group by bi.date_at""", nativeQuery = true)
    List<IDateSumPrice> findDateSumPriceByUserGroupAndCurrencyIdAndType(
            UUID userGroup, UUID currencyId, String type);

    @Query(value = """
        select coalesce(
          sum(case when k.type = 'PROFIT' then bi.price else 0.0 end) -
          sum(case when k.type = 'SPENDING' then bi.price else 0.0 end), 0.0)
        from budget_items bi
        join kinds k on bi.kind_id = k.id
        where bi.user_group = :userGroup and bi.currency_id = :currencyId
         and bi.date_at < :date""", nativeQuery = true)
    Double getRemainByDefaultCurrencyForDate(UUID userGroup, UUID currencyId, LocalDate date);

}

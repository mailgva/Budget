package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ICurrencyRepository extends JpaRepository<Currency, UUID> {
    @Transactional
    @Modifying
    void deleteByUserGroupAndId(UUID userGroup, UUID id);

    Currency findByUserGroupAndId(UUID userGroup, UUID id);

    @Query(value = """
        select c.* from currencies c
        where c.user_group = :userGroup and lower(c.name) = lower(:name)""", nativeQuery = true)
    Currency findByUserGroupAndName(UUID userGroup, String name);

    List<Currency> findAllByUserGroupOrderByName(UUID userGroup);

    @Query(value = """
        select c.* from currencies c
        where c.user_group = :userGroup and coalesce(c.hidden, false) = false
        order by c.name""", nativeQuery = true)
    List<Currency> findAllVisibleByUserGroupOrderByName(UUID userGroup);

}

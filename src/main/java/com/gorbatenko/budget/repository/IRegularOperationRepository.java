package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.RegularOperation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IRegularOperationRepository extends JpaRepository<RegularOperation, UUID> {
    @Transactional
    @Modifying
    void deleteByUserGroupAndId(UUID userGroup, UUID id);

    RegularOperation findByUserGroupAndId(UUID userGroup, UUID id);

    List<RegularOperation> findAllByUserGroupOrderByEvery(UUID userGroup);

    List<RegularOperation> findByUserGroupAndCurrencyId(UUID userGroup, UUID currencyId);

    List<RegularOperation> findByUserGroupAndKindId(UUID userGroup, UUID kindId);
}

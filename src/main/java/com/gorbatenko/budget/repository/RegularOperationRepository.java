package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.RegularOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;

@Repository
@RequiredArgsConstructor
public class RegularOperationRepository {
    private final IRegularOperationRepository repository;

    public RegularOperation save(RegularOperation regularOperation) {
        if (regularOperation.getUserGroup() == null) {
            regularOperation.setUserGroup(getUserGroup());
            if (!StringUtils.hasText(regularOperation.getDescription())) {
                regularOperation.setDescription(null);
            }
        }
        return repository.save(regularOperation);
    }

    public void deleteById(UUID id) {
        repository.deleteByUserGroupAndId(getUserGroup(), id);
    }

    public List<RegularOperation> findAll() {
        return repository.findAllByUserGroupOrderByEvery(getUserGroup());
    }

    public List<RegularOperation> adminFindAll() {
        return repository.findAll();
    }

    public RegularOperation findById(UUID id) {
        return repository.findByUserGroupAndId(getUserGroup(), id);
    }

    public List<RegularOperation> findByCurrencyId(UUID currencyId) {
        return repository.findByUserGroupAndCurrencyId(getUserGroup(), currencyId);
    }

    public List<RegularOperation> findByKindId(UUID kindId) {
        return repository.findByUserGroupAndKindId(getUserGroup(), kindId);
    }
}

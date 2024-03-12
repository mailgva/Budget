package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;

@Repository
@RequiredArgsConstructor
public class CurrencyRepository {
    private final ICurrencyRepository repository;

    public Currency save(Currency currency) {
        if (currency.getUserGroup() == null) {
            currency.setUserGroup(getUserGroup());
        }
        return repository.save(currency);
    }

    public void deleteById(UUID id) {
        repository.deleteByUserGroupAndId(getUserGroup(), id);
    }

    public List<Currency> findAll() {
        return repository.findAllByUserGroupOrderByName(getUserGroup());
    }

    public List<Currency> findAllVisible() {
        return repository.findAllVisibleByUserGroupOrderByName(getUserGroup());
    }

    public Currency findById(UUID id) {
        return repository.findByUserGroupAndId(getUserGroup(), id);
    }

    public Currency findByName(String name) {
        return repository.findByUserGroupAndName(getUserGroup(), name);
    }

}

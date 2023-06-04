package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.repository.CurrencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyService {

    private CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public Currency save(Currency currency) {
        return currencyRepository.save(currency);
    }

    public Currency getById(String id) {
        return currencyRepository.getById(id);
    }

    public List<Currency> getAll() {
        return currencyRepository.getAll();
    }

    public List<Currency> getVisibled() {
        return currencyRepository.getVisibled();
    }

    public List<Currency> getByHidden(boolean hidden) {
        return currencyRepository.getFilteredData(null, null, hidden);
    }

    public void deleteById(String id) {
        currencyRepository.deleteById(id);;
    }

    public Currency getByName(String name) {
        return currencyRepository.getByName(name);
    }
}

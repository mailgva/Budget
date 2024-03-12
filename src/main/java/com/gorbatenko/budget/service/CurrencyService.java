package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public Currency save(Currency currency) {
        return currencyRepository.save(currency);
    }

    public Currency findById(UUID id) {
        return currencyRepository.findById(id);
    }

    public List<Currency> findAll() {
        return currencyRepository.findAll();
    }

    public List<Currency> findAllVisible() {
        return currencyRepository.findAllVisible();
    }

    public void deleteById(UUID id) {
        currencyRepository.deleteById(id);;
    }

    public Currency findByName(String name) {
        return currencyRepository.findByName(name);
    }
}

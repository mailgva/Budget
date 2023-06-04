package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.util.CurrencyCount;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/")
public class DictionaryController extends AbstractWebController {
    @GetMapping
    public String getDictionaries(Model model) {
        model.addAttribute("pageName", "Справочники");
        return "dictionaries/dictionaries";
    }

    @GetMapping("{name}")
    public String getDictionary(@PathVariable("name") String name, Model model) {
        Dictionary dictionary = Dictionary.valueOf(name.toUpperCase());
        switch(dictionary) {
            case KINDS:
                List<BudgetItem> budgetItems = budgetItemService.getAll();
                TreeMap<Type, List<Kind>> mapKind = new TreeMap<>(getKinds().stream()
                        .collect(Collectors.groupingBy(Kind::getType)));

                Map<String, Long> mapCountKind = budgetItems.stream()
                        .collect(Collectors.groupingBy(b -> b.getKind().getId(), Collectors.counting()));

                model.addAttribute("mapKind", mapKind);
                model.addAttribute("mapCountKind", mapCountKind);
                model.addAttribute("pageName", "Виды приходов//расходов");
                return "dictionaries/kinds/kinds";
            case CURRENCIES:
                TreeMap<Currency, Long> currencies = new TreeMap<>();
                List<Currency> allCurrencies = currencyService.getAll();
                List<CurrencyCount> currencyCounts = budgetItemService.getCurrencyCounts();
                for (Currency currency : allCurrencies) {
                    for (CurrencyCount currencyCount : currencyCounts) {
                        if(currency.equals(currencyCount.getCurrency())) {
                            currencies.put(currency, currencyCount.getCount());
                            break;
                        }
                    }
                    currencies.putIfAbsent(currency, 0L);
                }

                model.addAttribute("currencies", currencies);
                model.addAttribute("pageName", "Валюты");
                return "dictionaries/currencies/currencies";
            default:
                return getDictionaries(model);
        }

    }
}

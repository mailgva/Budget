package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Dictionary;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/")
public class DictionaryController extends AbstractWebController {
    @GetMapping("/")
    public String getDictionaries(Model model) {
        model.addAttribute("pageName", "Справочники");
        return "dictionaries/dictionaries";
    }

    @GetMapping("/{name}")
    public String getDictionary(@PathVariable("name") String name, Model model) {
        User user = SecurityUtil.get().getUser();
        Dictionary dictionary = Dictionary.valueOf(name.toUpperCase());
        switch(dictionary) {
            case KINDS:

                List<Budget> budgets = budgetRepository.getAllByUserGroup(user.getGroup());

                TreeMap<Type, List<Kind>> mapKind = new TreeMap<>();
                mapKind.putAll(
                        getKinds().stream()
                                .collect(Collectors.groupingBy(Kind::getType)));

                Map<String, Long> mapCountKind = budgets.stream()
                        .collect(Collectors.groupingBy(b -> b.getKind().getId(), Collectors.counting()));

                model.addAttribute("mapKind", mapKind);
                model.addAttribute("mapCountKind", mapCountKind);
                model.addAttribute("pageName", "Виды приходов//расходов");
                return "/dictionaries/kinds/kinds";
            case CURRENCIES:
                List<Currency> currencies = getCurrencies();
                Collections.sort(currencies, Comparator.comparing(o -> o.getName()));
                model.addAttribute("currencies", currencies);
                model.addAttribute("pageName", "Валюты");
                return "/dictionaries/currencies/currencies";
            default:
                return getDictionaries(model);
        }

    }
}

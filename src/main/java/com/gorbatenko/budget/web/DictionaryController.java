package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Dictionary;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/")
public class DictionaryController extends AbstractWebController {
    public DictionaryController(CurrencyService currencyService, KindService kindService, BudgetItemService budgetItemService,
                                RegularOperationService regularOperationService, UserService userService, JoinRequestService joinRequestService) {
        super(currencyService, kindService, budgetItemService, regularOperationService, userService, joinRequestService);
    }

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
                List<BudgetItem> budgetItems = budgetItemService.findAll();
                TreeMap<Type, List<Kind>> mapKind = new TreeMap<>(getKinds().stream()
                        .collect(Collectors.groupingBy(Kind::getType)));

                Map<UUID, Long> mapCountKind = budgetItems.stream()
                        .collect(Collectors.groupingBy(b -> b.getKind().getId(), Collectors.counting()));

                model.addAttribute("mapKind", mapKind);
                model.addAttribute("mapCountKind", mapCountKind);
                model.addAttribute("pageName", "Виды приходов//расходов");
                return "dictionaries/kinds/kinds";
            case CURRENCIES:
                model.addAttribute("currencies", budgetItemService.getCurrencyCounts());
                model.addAttribute("pageName", "Валюты");
                return "dictionaries/currencies/currencies";
            default:
                return getDictionaries(model);
        }

    }
}

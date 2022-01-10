package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.CurrencyRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.repository.RegularOperationRepository;
import com.gorbatenko.budget.service.UserService;
import com.gorbatenko.budget.util.SecurityUtil;
import com.gorbatenko.budget.util.TypePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class AbstractWebController {
    private static final int POPULARKIND_COUNT = 5;

    protected static final LocalDateTime MIN_DATE_TIME = LocalDateTime.of(0, 1, 1, 0,0,1);
    protected static final LocalDateTime MAX_DATE_TIME = LocalDateTime.of(3000, 1, 1, 0,0,1);

    @Autowired
    protected BudgetRepository budgetRepository;

    @Autowired
    protected KindRepository kindRepository;

    @Autowired
    protected CurrencyRepository currencyRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected RegularOperationRepository regularOperationRepository;

    @ModelAttribute("userName")
    protected String getUserName(@AuthenticationPrincipal AuthorizedUser authUser){
        if (authUser == null) {
            return null;
        }
        return SecurityUtil.authUserName();
    }

    @ModelAttribute("defaultCurrencyName")
    protected String getDefaultCurrency(@AuthenticationPrincipal AuthorizedUser authUser){
        if (authUser == null) {
            return null;
        }
        return SecurityUtil.getCurrencyDefault().getName();
    }

    @PreAuthorize("isAuthenticated()")
    protected List<Kind> getKinds() {
        return kindRepository.getAll();
    }

    @ModelAttribute("listOfCurrencies")
    protected List<Currency> getCurrencies(@AuthenticationPrincipal AuthorizedUser authUser) {
        if (authUser == null) {
            return null;
        }
        return currencyRepository.getAll();
    }

    protected List<Kind> sortKindsByPopular(List<Kind> listKind, Type type, LocalDateTime startDate, LocalDateTime endDate) {
        List<Budget> listBudget = budgetRepository.getFilteredData(startDate, endDate, null, type.name(), null, null, null, TypePeriod.SELECTED_PERIOD);

        LinkedHashMap<Kind, Long> mapKindCount = new LinkedHashMap<>();
        listBudget.stream()
                .collect(Collectors.groupingBy(Budget::getKind, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(x -> x.getValue() >= POPULARKIND_COUNT)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> mapKindCount.put(x.getKey(), x.getValue()));

        List<Kind> result = new ArrayList<>(mapKindCount.keySet());

        for(Kind kind : listKind) {
            if (!result.contains(kind)) {
                result.add(kind);
            }
        }

        return result;
    }

    protected Model getBalanceParts(Model model, List<Budget> budgets, LocalDateTime startDate, LocalDateTime endDate) {
        Double profit = budgets.stream()
                .filter(b -> b.getKind().getType().equals(Type.PROFIT))
                .mapToDouble(Budget::getPrice)
                .sum();

        Double spending = budgets.stream()
                .filter(b -> b.getKind().getType().equals(Type.SPENDING))
                .mapToDouble(Budget::getPrice)
                .sum();

        Double remain = profit - spending;

        model.addAttribute("profit", profit);
        model.addAttribute("spending", spending);
        model.addAttribute("remain", remain);
        model.addAttribute("remainOnStartPeriod", getRemainOnStartPeriod(startDate));
        model.addAttribute("remainOnEndPeriod", getRemainOnStartPeriod(endDate));

        return model;
    }

    private Double getRemainOnStartPeriod(LocalDateTime startDate) {
        List<Budget> budgets =
                budgetRepository.getFilteredData(null, startDate, null, null, null, null, null, TypePeriod.SELECTED_PERIOD);
        return budgets.stream()
                .map(budget ->
                        (budget.getKind().getType().equals(Type.PROFIT) ? budget.getPrice() : budget.getPrice() * -1.D)).mapToDouble(Double::doubleValue).sum();
    }
}

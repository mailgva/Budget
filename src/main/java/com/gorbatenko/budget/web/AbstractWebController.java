package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.CurrencyRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.repository.RegularOperationRepository;
import com.gorbatenko.budget.service.UserService;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.BaseUtil.setTimeZoneOffset;


public class AbstractWebController {
    int POPULARKIND_COUNT = 5;

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
    protected String getUserName(){
        try {
            return  SecurityUtil.authUserName();
        } catch (Exception e) {
            return null;
        }
    }

    @ModelAttribute("defaultCurrencyName")
    protected String getDefaultCurrency(){
        try {
            return  SecurityUtil.get().getUser().getCurrencyDefault().getName();
        } catch (Exception e) {
            return null;
        }
    }

    protected List<Kind> getKinds() {
        User user = SecurityUtil.get().getUser();
        return kindRepository.getKindByUserGroupOrderByTypeAscNameAsc(user.getGroup());
    }

    @ModelAttribute("listOfCurrencies")
    protected List<Currency> getCurrencies() {
        try {
            User user = SecurityUtil.get().getUser();
            return currencyRepository.getCurrencyByUserGroupOrderByNameAsc(user.getGroup());
        } catch (Exception e) {
            return null;
        }
    }

    protected List<Kind> sortKindsByPopular(List<Kind> listKind, Type type, LocalDateTime startDate, LocalDateTime endDate, String userGroup) {

        List<Budget> listBudget = filterBudgetByUserCurrencyDefault(
                budgetRepository.getAllByKindTypeAndDateBetweenAndUser_Group(type, startDate, endDate, userGroup));
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

    protected Model getBalanceByKind(Model model, Kind kind) {
        User user = SecurityUtil.get().getUser();
        return getBalanceParts(model, filterBudgetByUserCurrencyDefault(
                budgetRepository.getBudgetByKindAndUser_Group(kind, user.getGroup())));
    }

    protected Model getBalanceByDate(Model model, LocalDate date) {
        User user = SecurityUtil.get().getUser();
        return getBalanceParts(model, filterBudgetByUserCurrencyDefault(
                budgetRepository.getBudgetByDateAndUser_Group(setTimeZoneOffset(date), user.getGroup())));
    }

    protected Model getBalanceParts(Model model, List<Budget> budgets) {
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
        model.addAttribute("remainOnStartPeriod", getRemainOnStartPeriod(budgets));
        model.addAttribute("remainOnEndPeriod", getRemainOnEndPeriod(budgets));

        return model;
    }

    protected Double getRemainOnStartPeriod(List<Budget> budgets) {
        if (budgets.isEmpty()) {
            return 0.0D;
        }

        String userGroup = SecurityUtil.get().getUser().getGroup();
        LocalDateTime startDate = budgets.stream().map(Budget::getDate).min(LocalDateTime::compareTo).get();

        List<Budget> budgetsLessStart = filterBudgetByUserCurrencyDefault(
                budgetRepository.getBudgetByUser_GroupAndDateLessThan(userGroup, startDate));

        return budgetsLessStart.stream()
                .map(budget ->
                        (budget.getKind().getType().equals(Type.PROFIT) ? budget.getPrice() : budget.getPrice() * -1.D)).mapToDouble(Double::doubleValue).sum();

    }

    protected Double getRemainOnEndPeriod(List<Budget> budgets) {
        if (budgets.isEmpty()) {
            return 0.0D;
        }

        String userGroup = SecurityUtil.get().getUser().getGroup();
        LocalDateTime endDate = budgets.stream().map(Budget::getDate).max(LocalDateTime::compareTo).get();

        List<Budget> budgetsLessOrEqualsEnd = filterBudgetByUserCurrencyDefault(
                budgetRepository.getBudgetByUser_GroupAndDateLessThanEqual(userGroup, endDate));

        return budgetsLessOrEqualsEnd.stream()
                .map(budget ->
                        (budget.getKind().getType().equals(Type.PROFIT) ? budget.getPrice() : budget.getPrice() * -1.D)).mapToDouble(Double::doubleValue).sum();

    }

    protected List<Budget> filterBudgetByUserCurrencyDefault(List<Budget> budgets) {
        Currency userCurrencyDefault = SecurityUtil.get().getUser().getCurrencyDefault();
        return budgets.stream()
                .filter(budget -> budget.getCurrency().getId().equals(userCurrencyDefault.getId()))
                .collect(Collectors.toList());
    }


}

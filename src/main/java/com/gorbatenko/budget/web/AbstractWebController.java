package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.service.*;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class AbstractWebController {
    private static final int POPULAR_KIND_COUNT = 5;

    @Autowired
    protected CurrencyService currencyService;

    @Autowired
    protected KindService kindService;

    @Autowired
    protected BudgetItemService budgetItemService;

    @Autowired
    protected RegularOperationService regularOperationService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected JoinRequestService joinRequestService;

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
        return kindService.getAll();
    }

    @ModelAttribute("listOfCurrencies")
    protected List<Currency> getCurrencies(@AuthenticationPrincipal AuthorizedUser authUser) {
        if (authUser == null) {
            return null;
        }
        return currencyService.getVisibled();
    }

    protected List<Kind> sortKindsByPopular(List<Kind> listKind, Type type, LocalDateTime startDate, LocalDateTime endDate) {
        List<BudgetItem> listBudgetItems = budgetItemService.getPopularByTypeForPeriod(startDate, endDate, type.name());

        LinkedHashMap<Kind, Long> mapKindCount = new LinkedHashMap<>();
        listBudgetItems.stream()
                .collect(Collectors.groupingBy(BudgetItem::getKind, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(x -> x.getValue() >= POPULAR_KIND_COUNT)
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

    protected void getBalanceParts(Model model, List<BudgetItem> budgetItems, LocalDateTime startDate, LocalDateTime endDate) {
        Double profit = budgetItems.stream()
                .filter(b -> b.getKind().getType().equals(Type.PROFIT))
                .mapToDouble(BudgetItem::getPrice)
                .sum();

        Double spending = budgetItems.stream()
                .filter(b -> b.getKind().getType().equals(Type.SPENDING))
                .mapToDouble(BudgetItem::getPrice)
                .sum();

        Double remain = profit - spending;

        model.addAttribute("profit", profit);
        model.addAttribute("spending", spending);
        model.addAttribute("remain", remain);
        model.addAttribute("remainOnStartPeriod", budgetItemService.getRemainByDefaultCurrencyForDate(startDate.toLocalDate().plusDays(1))); ;
        model.addAttribute("remainOnEndPeriod", budgetItemService.getRemainByDefaultCurrencyForDate(endDate.toLocalDate()));
    }
}

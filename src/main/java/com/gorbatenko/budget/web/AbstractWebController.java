package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.service.*;
import com.gorbatenko.budget.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class AbstractWebController {

    protected final CurrencyService currencyService;

    protected final KindService kindService;

    protected final BudgetItemService budgetItemService;

    protected final RegularOperationService regularOperationService;

    protected final UserService userService;

    protected final JoinRequestService joinRequestService;

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
        return kindService.findAll();
    }

    @ModelAttribute("listOfCurrencies")
    protected List<Currency> getCurrencies(@AuthenticationPrincipal AuthorizedUser authUser) {
        if (authUser == null) {
            return null;
        }
        return currencyService.findAllVisible();
    }

    protected void getBalanceParts(Model model, List<BudgetItem> budgetItems, LocalDate startDate, LocalDate endDate) {
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
        model.addAttribute("remainOnStartPeriod", budgetItemService.getRemainByDefaultCurrencyForDate(startDate)); ;
        model.addAttribute("remainOnEndPeriod", budgetItemService.getRemainByDefaultCurrencyForDate(endDate));
    }
}

package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.service.UserService;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.util.List;

import static com.gorbatenko.budget.util.BaseUtil.setTimeZoneOffset;


public class AbstractWebController {
    @Autowired
    protected BudgetRepository budgetRepository;

    @Autowired
    protected KindRepository kindRepository;

    @Autowired
    protected UserService userService;

    @ModelAttribute("userName")
    protected String getUserName(){
        try {
            return  SecurityUtil.authUserName();
        } catch (Exception e) {
            return null;
        }
    }

    protected List<Kind> getKinds() {
        User user = SecurityUtil.get().getUser();
        return kindRepository.findByUserGroupOrderByTypeAscNameAsc(user.getGroup());
    }

    protected Model getBalanceByKind(Model model, Kind kind) {
        User user = SecurityUtil.get().getUser();
        return getBalanceParts(model, budgetRepository.getBudgetBykindAndUser_Group(kind, user.getGroup()));
    }

    protected Model getBalanceByDate(Model model, LocalDate date) {
        User user = SecurityUtil.get().getUser();
        return getBalanceParts(model, budgetRepository.getBudgetByDateAndUser_Group(setTimeZoneOffset(date), user.getGroup()));
    }

    protected Model getBalanceParts(Model model, List<Budget> budgets) {
        Double profit = budgets.stream()
                .filter(b -> b.getKind().getType().equals(Type.PROFIT))
                .mapToDouble(budget -> budget.getPrice())
                .sum();

        Double spending = budgets.stream()
                .filter(b -> b.getKind().getType().equals(Type.SPENDING))
                .mapToDouble(budget -> budget.getPrice())
                .sum();

        Double remain = profit - spending;

        model.addAttribute("profit", profit);
        model.addAttribute("spending", spending);
        model.addAttribute("remain", remain);

        return model;
    }
}

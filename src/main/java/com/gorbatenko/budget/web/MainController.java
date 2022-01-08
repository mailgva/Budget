package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.Budget;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.BaseUtil.*;

@Controller
public class MainController extends AbstractWebController {

    @GetMapping("/")
    public String getMain(@AuthenticationPrincipal AuthorizedUser authUser) {
        if(authUser == null) {
            return "login";
        } else {
            return "redirect:/menu";
        }
    }

    @GetMapping("/budget")
    public String getMainBudget(@AuthenticationPrincipal AuthorizedUser authUser) {
        if(authUser == null) {
            return "login";
        } else {
            return "redirect:/menu";
        }
    }

    @GetMapping("/login")
    public String loginPage(@AuthenticationPrincipal AuthorizedUser authUser, Model model) {
        if(authUser == null) {
            model.addAttribute("pageName", "Вход");
            return "login";
        } else {
            return "redirect:/menu";
        }
    }

    @GetMapping("/login-error")
    public String login(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            AuthenticationException ex = (AuthenticationException) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
            }
        }
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("pageName", "Ошибка - неверные учетные данные");
        return "login";
    }

    @GetMapping("/menu")
    public String getMenu(Model model, HttpServletRequest request) {
        List<Budget> listBudget = budgetRepository.getAll();

        LocalDate lastActivity = listBudget.stream()
                .map(Budget::getDate)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now())
                .toLocalDate();

        String lastGroupActivityDate = dateToStr(lastActivity);
        String lastGroupActivityDateCustom = dateToStrCustom(lastActivity, "dd-MM-yyyy");

        model = getBalanceParts(model, listBudget, MIN_DATE_TIME, MAX_DATE_TIME);

        int sumTimezoneOffsetMinutes = BudgetController.getSumTimezoneOffsetMinutes(request);

        LocalDate now = LocalDate.now();

        LocalDateTime startLocalDate = LocalDateTime.of(now, LocalTime.MIN).plusMinutes(sumTimezoneOffsetMinutes);
        LocalDateTime endLocalDate = LocalDateTime.of(now, LocalTime.MAX).plusMinutes(sumTimezoneOffsetMinutes);

        listBudget = listBudget.stream()
                .filter(budget -> budget.getDate().isAfter(startLocalDate) && budget.getDate().isBefore(endLocalDate))
                .sorted(Comparator.comparing(Budget::getCreateDateTime))
                .collect(Collectors.toList());

        TreeMap<LocalDate, List<Budget>> map = listBudgetToTreeMap(listBudget);

        model.addAttribute("lastGroupActivityDate", lastGroupActivityDate);
        model.addAttribute("lastGroupActivityDateCustom", lastGroupActivityDateCustom);
        model.addAttribute("listBudget", map);
        return "menu";
    }

}

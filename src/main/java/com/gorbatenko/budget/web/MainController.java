package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.util.TypePeriod;
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
        LocalDate lastActivity = budgetRepository.getMaxDate().toLocalDate();

        String lastGroupActivityDate = dateToStr(lastActivity);
        String lastGroupActivityDateCustom = dateToStrCustom(lastActivity, "dd-MM-yyyy");

        Double profit = budgetRepository.getSumPriceByType(Type.PROFIT);
        Double spending = budgetRepository.getSumPriceByType(Type.SPENDING);
        model.addAttribute("profit", profit);
        model.addAttribute("spending", spending);
        model.addAttribute("remain", profit-spending);

        int sumTimezoneOffsetMinutes = BudgetController.getSumTimezoneOffsetMinutes(request);

        LocalDateTime timeZoneOffset = LocalDateTime.now().plusMinutes(sumTimezoneOffsetMinutes);

        LocalDateTime startLocalDate = setTimeZoneOffset(timeZoneOffset.toLocalDate());
        LocalDateTime endLocalDate = setTimeZoneOffset(timeZoneOffset.toLocalDate());

        List<Budget> listBudget =
                budgetRepository.getFilteredData(startLocalDate, endLocalDate, null, null, null, null, null, TypePeriod.SELECTED_PERIOD)
                .stream()
                .sorted(Comparator.comparing(Budget::getCreateDateTime))
                .collect(Collectors.toList());

        TreeMap<LocalDate, List<Budget>> map = listBudgetToTreeMap(listBudget);

        model.addAttribute("lastGroupActivityDate", (LocalDate.MIN.equals(lastActivity) ? "" : lastGroupActivityDate));
        model.addAttribute("lastGroupActivityDateCustom", (LocalDate.MIN.equals(lastActivity) ? "" : lastGroupActivityDateCustom));
        model.addAttribute("listBudget", map);
        model.addAttribute("joinRequests", joinRequestRepository.getNewJoinRequests());
        return "menu";
    }

}

package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.BaseUtil.*;

@Controller
public class MainController extends BaseWebController {

    private final JoinRequestService joinRequestService;

    public MainController(CurrencyService currencyService, KindService kindService, BudgetItemService budgetItemService,
                          JoinRequestService joinRequestService) {
        super(currencyService, kindService, budgetItemService);
        this.joinRequestService = joinRequestService;
    }

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
    public String getMenu(Model model) {
        LocalDate lastActivity =  budgetItemService.findMaxDate();
        UUID lastCurrencyId = null;
        String lastGroupActivityDate = "";
        String lastGroupActivityDateCustom = "";
        if (!lastActivity.isEqual(LocalDate.MIN)) {
            lastCurrencyId = budgetItemService.findLastCurrencyIdByDate(lastActivity);
            lastGroupActivityDate = dateToStr(lastActivity);
            lastGroupActivityDateCustom = dateToStrCustom(lastActivity, "dd-MM-yyyy");
        }


        Double profit = budgetItemService.getSumPriceByDefaultCurrencyAndType(Type.PROFIT);
        Double spending = budgetItemService.getSumPriceByDefaultCurrencyAndType(Type.SPENDING);
        model.addAttribute("profit", profit);
        model.addAttribute("spending", spending);
        model.addAttribute("remain", profit-spending);

        LocalDate today = LocalDate.now();

        List<BudgetItem> listBudgetItems =
                budgetItemService.findBySelectedPeriod(today, today)
                .stream()
                .sorted(Comparator.comparing(BudgetItem::getCreatedAt))
                .collect(Collectors.toList());

        TreeMap<LocalDate, List<BudgetItem>> map = listBudgetToTreeMap(listBudgetItems);

        model.addAttribute("lastCurrencyId", lastCurrencyId);
        model.addAttribute("lastGroupActivityDate", (LocalDate.MIN.isEqual(lastActivity) ? "" : lastGroupActivityDate));
        model.addAttribute("lastGroupActivityDateCustom", (LocalDate.MIN.isEqual(lastActivity) ? "" : lastGroupActivityDateCustom));
        model.addAttribute("listBudgetItems", map);
        model.addAttribute("joinRequests", joinRequestService.getNewJoinRequests());
        return "menu";
    }

}

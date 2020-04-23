package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

import static com.gorbatenko.budget.util.BaseUtil.*;

@Controller
@RequestMapping(value = "/")
public class MainController extends AbstractWebController {

    @GetMapping("/")
    public String getMain(@AuthenticationPrincipal AuthorizedUser authUser) {
        if(authUser == null) {
            return "login";
        } else {
            return "redirect:menu";
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
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
        return "login";
    }

    @GetMapping("/menu")
    public String getMenu(Model model) {
        User user = SecurityUtil.get().getUser();
        List<Budget> listBudget = budgetRepository.getBudgetByUser_GroupOrderByDateDesc(user.getGroup());

        String lastGroupActivityDate = dateToStr(listBudget.stream()
                .map(Budget::getCreateDateTime)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now())
                .toLocalDate());

        model = getBalanceParts(model, filterBudgetByUserCurrencyDefault(listBudget));

        listBudget = budgetRepository
                .getBudgetByDateAndUser_Group(
                        setTimeZoneOffset(LocalDate.now()), user.getGroup());
        TreeMap<LocalDate, List<Budget>> map = listBudgetToTreeMap(listBudget);

        model.addAttribute("lastGroupActivityDate", lastGroupActivityDate);
        model.addAttribute("listBudget", map);
        return "menu";
    }

}

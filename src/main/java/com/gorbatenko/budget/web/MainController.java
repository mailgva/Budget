package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
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

@Controller
@RequestMapping(value = "/")
public class MainController extends AbstractWebController {

    @GetMapping("/")
    public String getMain(@AuthenticationPrincipal AuthorizedUser authUser) {
        if(authUser == null) {
            return "main";
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
        model = getBalanceParts(model, filterBudgetByUserCurrencyDefault(
                budgetRepository.getBudgetByUser_GroupOrderByDateDesc(user.getGroup())));
        return "menu";
    }

}

package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Role;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.util.SecurityUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "/profile/")
public class ProfileController extends AbstractWebController {

    @Autowired
    AuthenticationManager authenticationManager;

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model) {
        User user = SecurityUtil.get().getUser();

        List<User> usersGroup = userService.getByGroup(user.getGroup());
        String groupMembers = usersGroup.stream()
                .map(u -> u.getName())
                .collect(Collectors.joining(", "));

        Map<Currency, Boolean> mapCurrencies = new HashMap<>();

        currencyRepository.getCurrencyByUserGroupOrderByNameAsc(user.getGroup())
                .forEach(currency -> mapCurrencies.put(currency, currency.getId().equals(user.getCurrencyDefault().getId())));

        model.addAttribute("user", user);
        model.addAttribute("groupMembers", groupMembers);
        model.addAttribute("mapCurrencies", mapCurrencies);
        model = getBalanceParts(model, filterBudgetByUserCurrencyDefault(
                budgetRepository.getBudgetByUser_GroupOrderByDateDesc(user.getGroup())));
        model.addAttribute("pageName", "Профиль");
        return "profile/profile";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageName", "Регистрация");
        return "/profile/register";
    }

    @PostMapping("/register")
    public String newUser(@ModelAttribute User user, Model model) throws Exception{
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        try {
            user = userService.create(user);
            UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,
                        userDetails.getPassword(), userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "/profile/register";
        }
        return "redirect:/menu";
    }

    @SneakyThrows
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/jointogroup/{id}")
    public String joinToGroup(@PathVariable("id") String id) {
        List<User> groupUser = userService.getByGroup(id);
        if (groupUser.size() == 0) {
            throw new Exception(String.format("Невозможно присоедиться к группе!<br>Группы с идентификатором [%s] не существует!", id));
        }

        User user = SecurityUtil.get().getUser();
        User owner = userService.findById(id);
        user.setGroup(id);
        user.setCurrencyDefault(owner.getCurrencyDefault());
        userService.save(user);
        return "redirect:/profile/";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/changedefcurrency")
    public String changeDefaultCurrency(@RequestParam(value="currencyId", required=true) String currencyId) {
        User user = SecurityUtil.get().getUser();
        user.setCurrencyDefault(currencyRepository.getCurrencyByUserGroupAndId(user.getGroup(), currencyId));
        userService.save(user);
        return "redirect:/profile/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/changedefcurrency")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changeDefaultCurrencyGet(@RequestParam(value="currencyId", required=true) String currencyId) {
        User user = SecurityUtil.get().getUser();
        user.setCurrencyDefault(currencyRepository.getCurrencyByUserGroupAndId(user.getGroup(), currencyId));
        userService.save(user);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/changename")
    public String changeName(@RequestParam(value="username", required=true) String name) {
        User user = SecurityUtil.get().getUser();
        user.setName(name);
        userService.save(user);
        List<Budget> budgets = budgetRepository.getAllByUserId(user.getId());
        for(Budget budget : budgets) {
            budget.setUser(user);
            budgetRepository.save(budget);
        }
        return "redirect:/profile/";
    }
}

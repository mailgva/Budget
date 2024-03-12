package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.service.*;
import com.gorbatenko.budget.to.RemainderTo;
import com.gorbatenko.budget.to.UserTo;
import com.gorbatenko.budget.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.Utils.equalsUUID;

@Controller
@RequestMapping(value = "/profile/")
public class ProfileController extends AbstractWebController {

    public ProfileController(CurrencyService currencyService, KindService kindService, BudgetItemService budgetItemService,
                             RegularOperationService regularOperationService, UserService userService, JoinRequestService joinRequestService) {
        super(currencyService, kindService, budgetItemService, regularOperationService, userService, joinRequestService);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model) {
        reloadUserContext(SecurityUtil.get().getUser());
        User user = SecurityUtil.get().getUser();

        List<User> usersGroup = userService.findByUserGroup(user.getUserGroup()).stream()
                .sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toList());
        String groupMembers = usersGroup.stream()
                .map(u -> u.getName() + (equalsUUID(u.getId(), u.getUserGroup()) ? " (Админ)" : ""))
                .sorted()
                .collect(Collectors.joining(", "));

        Map<Currency, Boolean> mapCurrencies = new HashMap<>();

        currencyService.findAllVisible()
                .forEach(currency ->
                        mapCurrencies.put(currency, equalsUUID(currency.getId(), user.getCurrencyDefault().getId())));

        model.addAttribute("user", user);
        model.addAttribute("groupMembers", groupMembers);
        model.addAttribute("usersGroup", usersGroup);
        model.addAttribute("mapCurrencies", mapCurrencies);
        model.addAttribute("mapCurrencyRemainders", getCurrencyRemainders());
        model.addAttribute("pageName", "Профиль");
        return "profile/profile";
    }

    @GetMapping("register")
    public String register(Model model) {
        model.addAttribute("pageName", "Регистрация");
        return "/profile/register";
    }

    @PostMapping(value = "register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String newUser(@Valid @RequestBody UserTo user, Model model) {
        try {
            if (userService.findByEmail(user.getEmail()) != null) {
                model.addAttribute("error", "Пользователь с email '" + user.getEmail() + "' уже существует.");
                return "/profile/register";
            }
            User newUser = new User(user.getName(), user.getEmail(), user.getPassword(), Collections.singleton(Role.ROLE_USER));
            reloadUserContext(userService.create(newUser));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "/profile/register";
        }
        return "redirect:/menu";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("jointogroup/{groupId}")
    public String joinToGroup(@PathVariable("groupId") UUID groupId, RedirectAttributes rm) throws Exception {
        User user = SecurityUtil.get().getUser();
        List<User> groupUser = userService.findByUserGroup(groupId);
        if (groupUser.size() == 0 && !equalsUUID(user.getId(), groupId)) {
            throw new Exception(String.format("Невозможно присоедиться к группе!<br>Группы с идентификатором [%s] не существует!", groupId));
        }

        if (!equalsUUID(user.getId(), groupId)) {
            if (!joinRequestService.isExistsNoAnsweredRequest(groupId)) {
                JoinRequest joinRequest = new JoinRequest();
                joinRequest.setUserGroup(groupId);
                joinRequestService.save(joinRequest);
            }
            rm.addFlashAttribute("info", "Отправлен запрос на присоединение к группе. Ожидайте решения администратора группы.");
        } else {
            user.setUserGroup(groupId);
            userService.save(user);
            reloadUserContext(user);
        }
        return "redirect:/profile/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("removefromgroup/{userId}")
    public ResponseEntity removeFromGroup(@PathVariable("userId") UUID userId) {
        User user = userService.findById(userId);
        user.setUserGroup(userId);
        userService.save(user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void reloadUserContext(User user) {
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,
                userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    @GetMapping("joinrequest/{id}/accept")
    public ResponseEntity joinToGroupAccept(@PathVariable("id") UUID id) {
        User userAdmin = SecurityUtil.get().getUser();
        JoinRequest joinRequest = joinRequestService.getById(id);
        if (!equalsUUID(userAdmin.getId(), joinRequest.getUserGroup())) {
            return ResponseEntity.badRequest().build();
        }
        joinRequest.setAcceptedAt(LocalDateTime.now());
        joinRequestService.save(joinRequest);

        User user = joinRequest.getUser();
        user.setUserGroup(joinRequest.getUserGroup());
        user.setCurrencyDefault(userService.findById(joinRequest.getUserGroup()).getCurrencyDefault());
        userService.save(user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("joinrequest/{id}/decline")
    public ResponseEntity joinToGroupDecline(@PathVariable("id") UUID id) {
        User userAdmin = SecurityUtil.get().getUser();
        JoinRequest joinRequest = joinRequestService.getById(id);
        if (!equalsUUID(userAdmin.getId(), joinRequest.getUserGroup())) {
            return ResponseEntity.badRequest().build();
        }
        joinRequest.setDeclinedAt(LocalDateTime.now());
        joinRequestService.save(joinRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("changedefcurrency")
    public String changeDefaultCurrency(@RequestParam(value="currencyId") UUID currencyId) {
        userService.changeDefaultCurrency(currencyId);
        return "redirect:/profile/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("changedefcurrency")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changeDefaultCurrencyGet(@RequestParam(value="currencyId") UUID currencyId) {
        userService.changeDefaultCurrency(currencyId);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    @PostMapping("changename")
    public String changeName(@RequestParam(value="username") String name) {
        User user = SecurityUtil.get().getUser();
        user.setName(name);
        userService.save(user);
        return "redirect:/profile/";
    }

    private Map<String, RemainderTo> getCurrencyRemainders() {
        Map<String, RemainderTo> result = new HashMap<>();
        for (Currency currency : currencyService.findAllVisible()) {
            Double profit = budgetItemService.getSumPriceByCurrencyAndType(currency, Type.PROFIT);
            Double spending = budgetItemService.getSumPriceByCurrencyAndType(currency, Type.SPENDING);
            result.put(currency.getName(), new RemainderTo(profit, spending));
        }
        return result;
    }
}

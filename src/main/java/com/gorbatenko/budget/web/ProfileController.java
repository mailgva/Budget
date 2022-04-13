package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.util.SecurityUtil;
import com.gorbatenko.budget.util.TypePeriod;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/profile/")
public class ProfileController extends AbstractWebController {

    @Autowired
    AuthenticationManager authenticationManager;

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model) {
        reloadUserContext(SecurityUtil.get().getUser());
        User user = SecurityUtil.get().getUser();

        List<User> usersGroup = userService.getByGroup(user.getGroup()).stream()
                .sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toList());
        String groupMembers = usersGroup.stream()
                .map(u -> u.getName() + (u.getId().equals(u.getGroup()) ? " (Админ)" : ""))
                .sorted()
                .collect(Collectors.joining(", "));

        Map<Currency, Boolean> mapCurrencies = new HashMap<>();

        currencyRepository.getAll()
                .forEach(currency -> mapCurrencies.put(currency, currency.getId().equals(user.getCurrencyDefault().getId())));

        model.addAttribute("user", user);
        model.addAttribute("groupMembers", groupMembers);
        model.addAttribute("usersGroup", usersGroup);
        model.addAttribute("mapCurrencies", mapCurrencies);
        getBalanceParts(model, budgetRepository.getAll(), MIN_DATE_TIME, MAX_DATE_TIME);
        model.addAttribute("pageName", "Профиль");
        return "profile/profile";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageName", "Регистрация");
        return "/profile/register";
    }

    @PostMapping("/register")
    public String newUser(@ModelAttribute User user, Model model) {
        try {
            if (userService.findByEmail(user.getEmail()) != null) {
                model.addAttribute("error", "Пользователь с email '" + user.getEmail() + "' уже существует.");
                return "/profile/register";
            }
            user.setRoles(Collections.singleton(Role.ROLE_USER));
            reloadUserContext(userService.create(user));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "/profile/register";
        }
        return "redirect:/menu";
    }

    @SneakyThrows
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/jointogroup/{groupId}")
    public String joinToGroup(@PathVariable("groupId") String groupId, RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();
        List<User> groupUser = userService.getByGroup(groupId);
        if (groupUser.size() == 0 && !user.getId().equals(groupId)) {
            throw new Exception(String.format("Невозможно присоедиться к группе!<br>Группы с идентификатором [%s] не существует!", groupId));
        }

        if (!user.getId().equals(groupId)) {
            if (!joinRequestRepository.isExistsNoAnsweredRequest(groupId)) {
                JoinRequest joinRequest = new JoinRequest();
                joinRequest.setUserGroup(groupId);
                joinRequestRepository.save(joinRequest);
            }
            rm.addFlashAttribute("info", "Отправлен запрос на присоединение к группе. Ожидайте решения администратора группы.");
        } else {
            user.setGroup(groupId);
            userService.save(user);
            reloadUserContext(user);
        }
        return "redirect:/profile/";
    }

    @SneakyThrows
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/removefromgroup/{userId}")
    public ResponseEntity removeFromGroup(@PathVariable("userId") String userId) {
        User user = userService.findById(userId);
        user.setGroup(userId);
        userService.save(user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void reloadUserContext(User user) {
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,
                userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/joinrequest/{id}/accept")
    public ResponseEntity joinToGroupAccept(@PathVariable("id") String id, Model model) {
        User userAdmin = SecurityUtil.get().getUser();
        JoinRequest joinRequest = joinRequestRepository.findById(id);
        if (!userAdmin.getId().equals(joinRequest.getUserGroup())) {
            return ResponseEntity.badRequest().build();
        }
        joinRequest.setAccepted(LocalDateTime.now());
        joinRequestRepository.save(joinRequest);

        User user = joinRequest.getUser();
        user.setGroup(joinRequest.getUserGroup());
        user.setCurrencyDefault(userService.findById(joinRequest.getUserGroup()).getCurrencyDefault());
        userService.save(user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/joinrequest/{id}/decline")
    public ResponseEntity joinToGroupDecline(@PathVariable("id") String id, Model model) {
        User userAdmin = SecurityUtil.get().getUser();
        JoinRequest joinRequest = joinRequestRepository.findById(id);
        if (!userAdmin.getId().equals(joinRequest.getUserGroup())) {
            return ResponseEntity.badRequest().build();
        }
        joinRequest.setDeclined(LocalDateTime.now());
        joinRequestRepository.save(joinRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/changedefcurrency")
    public String changeDefaultCurrency(@RequestParam(value="currencyId") String currencyId) {
        User user = SecurityUtil.get().getUser();
        user.setCurrencyDefault(currencyRepository.getById(currencyId));
        userService.save(user);
        return "redirect:/profile/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/changedefcurrency")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changeDefaultCurrencyGet(@RequestParam(value="currencyId") String currencyId) {
        User user = SecurityUtil.get().getUser();
        user.setCurrencyDefault(currencyRepository.getById(currencyId));
        userService.save(user);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/changename")
    public String changeName(@RequestParam(value="username") String name) {
        User user = SecurityUtil.get().getUser();
        user.setName(name);
        userService.save(user);
        List<Budget> budgets = budgetRepository.getFilteredData(null,null, user.getId(), null, null, null, null, TypePeriod.ALL_TIME);
        for(Budget budget : budgets) {
            budget.setUser(new com.gorbatenko.budget.model.doc.User(user.getId(), user.getName()));
            budgetRepository.save(budget);
        }
        return "redirect:/profile/";
    }
}

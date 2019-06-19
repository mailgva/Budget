package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Role;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.service.UserService;
import com.gorbatenko.budget.to.BudgetTo;
import com.gorbatenko.budget.to.KindTo;
import com.gorbatenko.budget.util.SecurityUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@Controller
public class WebController {

    @Autowired
    BudgetRepository repository;

    @Autowired
    private KindRepository kindRepository;

    @Autowired
    private UserService userService;


    @GetMapping("/")
    public String getMain() {
        return "main";
    }


    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @RequestMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String newUser(@ModelAttribute User user) {
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        userService.create(user);
        return "redirect:login";
    }


    @GetMapping("/menu")
    public String getMenu(Model model) {
        User user = SecurityUtil.get().getUser();
        List<Budget> budgets = repository.getBudgetByUser_Group(user.getGroup());
        Double profit = budgets.stream()
            .mapToDouble(budget -> (budget.getKind().getType().equals(Type.PROFIT) ? budget.getPrice() : 0.0))
            .sum();

        Double spending = budgets.stream()
            .mapToDouble(budget -> (budget.getKind().getType().equals(Type.SPENDING) ? budget.getPrice() : 0.0))
            .sum();

        Double remain = budgets.stream()
                    .mapToDouble(budget -> (budget.getKind().getType().equals(Type.SPENDING) ? -1.0 : 1.0) * budget.getPrice())
                    .sum();

        model.addAttribute("profit", profit);
        model.addAttribute("spending", spending);
        model.addAttribute("remain", remain);
        return "menu";
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/jointogroup/{id}")
    public String joinToGroup(@PathVariable("id") String id) {
        User user = SecurityUtil.get().getUser();
        user.setGroup(id);
        userService.save(user);
        return "/menu";
    }


    @GetMapping("/profile")
    public String profile(Model model) {
        User user = SecurityUtil.get().getUser();

        List<User> usersGroup = userService.getByGroup(user.getGroup());
        String groupMembers = usersGroup.stream()
            .map(u -> u.getName())
            .collect(Collectors.joining(","));

        model.addAttribute("user", user);
        model.addAttribute("groupMembers", groupMembers);
        return "/profile";
    }


    @GetMapping("/statistic")
    public String getStatistic(Model model) {
        User user = SecurityUtil.get().getUser();
        List<Budget> listBudget = repository.getBudgetByUser_Group(user.getGroup());
        model.addAttribute("listBudget", listBudget);
        return "statistic";
    }

    @GetMapping("/statistic/{type}")
    public String getBudgetByType(@PathVariable("type") String type,  Model model) {
        Type value;
        try {
            value = Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            value = Type.SPENDING;
        }

        User user = SecurityUtil.get().getUser();
        model.addAttribute("listBudget",
                repository.getBudgetByKindTypeAndUser_GroupOrderByCreateDateTime(value, user.getGroup()));
        return "statistic";
    }


    @GetMapping("/create/{type}")
    public String create(@PathVariable("type") String type, Model model) {
        User user = SecurityUtil.get().getUser();
        model.addAttribute("type",  Type.valueOf(type.toUpperCase()));
        List<Kind> kinds = kindRepository.findByTypeAndUserGroup(Type.valueOf(type.toUpperCase()), user.getGroup());
        Collections.sort(kinds, Comparator.comparing(o -> o.getName()));
        model.addAttribute("kinds", kinds);

        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        model.addAttribute("date", localDate.format(formatter));

        return "create";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        Budget budget = repository.findById(id).get();
        User user = SecurityUtil.get().getUser();
        model.addAttribute("budget", budget );
        List<Kind> kinds = kindRepository.findByTypeAndUserGroup(budget.getKind().getType(), user.getGroup());
        Collections.sort(kinds, Comparator.comparing(o -> o.getName()));
        model.addAttribute("kinds", kinds);
        return "edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, Model model) {
        repository.deleteById(id);
        return getStatistic(model);
    }

    @GetMapping("/dictionaries")
    public String getDictionaries() {
        return "dictionaries";
    }

    @GetMapping("/dictionary/{name}")
    public String getDictionary(@PathVariable("name") String name, Model model) {
        if(name.equalsIgnoreCase("KINDS")) {
            User user = SecurityUtil.get().getUser();
            List<Kind> kinds = kindRepository.findByUserGroup(user.getGroup());
            Collections.sort(kinds, Comparator.comparing(o -> o.getType().getValue()));
            model.addAttribute("kinds", kinds);
        }
        return "kinds";
    }

    @GetMapping("/dictionary/kinds/create")
    public String createDicKind() {
        return "createDicKind";
    }

    @GetMapping("/dictionary/kinds/edit/{id}")
    public String editDicKind(@PathVariable("id") String id, Model model) {
        model.addAttribute("kind", kindRepository.findById(id).get());
        return "editDicKind";
    }

    @GetMapping("/dictionary/kinds/delete/{id}")
    public String deleteDicKind(@PathVariable("id") String id, Model model) {
        kindRepository.deleteById(id);
        return "redirect:/dictionary/kinds";
    }

    @PostMapping("/dictionary/kinds/create")
    public String createNewDicKind(@ModelAttribute KindTo kindTo) {
        Kind kind = createKindFromKindTo(kindTo);
        kind.setId(kindTo.getId());
        kindRepository.save(kind);
        return "redirect:/dictionary/kinds";
    }

    @PostMapping("/dictionary/kinds/edit")
    public String editNewDicKind(@ModelAttribute KindTo kindTo) {
        Kind kind = createKindFromKindTo(kindTo);
        kind.setId(kindTo.getId());
        kindRepository.save(kind);
        return "redirect:/dictionary/kinds";
    }

    @PostMapping("/")
    public String createNewBudgetItem(@ModelAttribute BudgetTo budgetTo) {
        Budget budget = createBudgetFromBudgetTo(budgetTo);
        budget.setId(budgetTo.getId());
        repository.save(budget);
        return "redirect:/statistic";
    }

    @GetMapping("/bygroup/{group}")
    public String getBudgetByGroup(@PathVariable("group") String group,  Model model) {
        model.addAttribute("listBudget", repository.getBudgetByUser_Group(group));
        return "statistic";
    }

    public Budget createBudgetFromBudgetTo(BudgetTo b) {
        Kind kind = kindRepository.findByNameIgnoreCase(b.getKind());
        User user = SecurityUtil.get().getUser();
        Budget budget = new Budget(user, kind, LocalDateTime.of(b.getDate(), LocalTime.MIN), b.getDescription(), b.getPrice());
        return budget;
    }

    private Kind createKindFromKindTo(KindTo kindTo) {
        User user = SecurityUtil.get().getUser();
        Kind kind = new Kind(kindTo.getType(), kindTo.getName(), user.getGroup());
        return kind;
    }

    @ModelAttribute("userName")
    private String getUserName(){
        try {
            return  SecurityUtil.authUserName();
        } catch (Exception e) {
            return null;
        }
    }

}

package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
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
import com.gorbatenko.budget.util.BaseUtil;
import com.gorbatenko.budget.util.SecurityUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.gorbatenko.budget.util.BaseUtil.listBudgetToTreeMap;
import static com.gorbatenko.budget.util.BaseUtil.setTimeZoneOffset;
import static com.gorbatenko.budget.util.SecurityUtil.hidePassword;


@Controller
public class WebController {

    @Autowired
    BudgetRepository budgetRepository;

    @Autowired
    private KindRepository kindRepository;

    @Autowired
    private UserService userService;

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

    @RequestMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("error", true);
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String newUser(@ModelAttribute User user, Model model) throws Exception{
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        try {
            userService.create(user);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "/register";
        }

        return "redirect:login";
    }


    @GetMapping("/menu")
    public String getMenu(Model model) {
        User user = SecurityUtil.get().getUser();
        model = getBalanceParts(model, budgetRepository.getBudgetByUser_GroupOrderByDateDesc(user.getGroup()));
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



    @GetMapping("/statistic/view/group")
    public String getStatisticView(@RequestParam(value = "startDate", required=false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate startDate,
                                   @RequestParam(value = "endDate", required=false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate endDate,
                                   Model model) {
        User user = SecurityUtil.get().getUser();

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        LocalDate now = LocalDate.now();

        if(startDate == null) {
            offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), 1), LocalTime.MIN);
            startDate = offSetStartDate.toLocalDate();
        }
        offSetStartDate = setTimeZoneOffset(startDate).minusDays(1);

        if(endDate == null) {
            offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth()), LocalTime.MAX);
            endDate = offSetEndDate.toLocalDate();
        }
        offSetEndDate = setTimeZoneOffset(endDate).plusDays(1);

        List<Budget> listBudget = hidePassword(budgetRepository.getBudgetByDateBetweenAndUser_Group(
            offSetStartDate, offSetEndDate, user.getGroup()));

        model = getBalanceParts(model, listBudget);

        Map<Type, Map<Kind, Double>> mapKind = listBudget.stream()
                .collect(Collectors.groupingBy(
                        budget ->
                            budget.getKind().getType(),(
                            Collectors.groupingBy(
                                    Budget::getKind,
                                    TreeMap::new,
                                    Collectors.summingDouble(Budget::getPrice)))));

        TreeMap<Type, Map<Kind, Double>> mapKindSort = new TreeMap<>();
        mapKindSort.putAll(mapKind);
        model.addAttribute("startDate", BaseUtil.dateToStr(startDate));
        model.addAttribute("endDate", BaseUtil.dateToStr(endDate));
        model.addAttribute("mapKind", mapKindSort);
        return "statgroup";
    }


    @GetMapping("/statistic")
    public String getStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate startDate,
                            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate endDate,
                            @RequestParam(value = "kindId", defaultValue = "-1") String id,
                            @RequestParam(value = "type", defaultValue = "allTypes") String typeStr,
                            @RequestParam(value = "comment", defaultValue = "") String comment, Model model) {

        User user = SecurityUtil.get().getUser();

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        LocalDate now = LocalDate.now();

        if(startDate == null) {
            offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), 1), LocalTime.MIN);
            startDate = offSetStartDate.toLocalDate();
        }
        offSetStartDate = setTimeZoneOffset(startDate).minusDays(1);

        if(endDate == null) {
            offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth()), LocalTime.MAX);
            endDate = offSetEndDate.toLocalDate();
        }
        offSetEndDate = setTimeZoneOffset(endDate).plusDays(1);

        Kind kind = new Kind();

        Type type;

        List<Budget> listBudget;


        if ("-1".equals(id)) {
            listBudget = hidePassword(budgetRepository.getBudgetByDateBetweenAndUser_Group(
                offSetStartDate, offSetEndDate, user.getGroup()));
        } else {
            kind = kindRepository.findKindByUserGroupAndId(user.getGroup(), id);
            listBudget = hidePassword(
                budgetRepository.getBudgetByKindAndDateBetweenAndUser_Group(kind,
                    offSetStartDate, offSetEndDate, user.getGroup()));
        }

        if (!("allTypes".equals(typeStr))) {
            type = Type.valueOf(typeStr);
            listBudget = listBudget.stream().
                filter(budget -> budget.getKind().getType().equals(type)).
                collect(Collectors.toList());
            model.addAttribute("typeName", type.getValue());
        }

        if(! comment.isEmpty()) {
            listBudget = listBudget.stream()
                .filter(budget -> budget.getDescription().toUpperCase().contains(comment.toLowerCase()))
                .collect(Collectors.toList());
        }

        model = getBalanceParts(model, listBudget);
        TreeMap<LocalDate, List<Budget>> map = listBudgetToTreeMap(listBudget);
        model.addAttribute("startDate", BaseUtil.dateToStr(startDate));
        model.addAttribute("endDate", BaseUtil.dateToStr(endDate));
        model.addAttribute("listBudget", map);
        model.addAttribute("kindList", getKinds());
        model.addAttribute("kindName", kind.getName());
        model.addAttribute("comment", comment);

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
        Budget budget = budgetRepository.findById(id).get();
        User user = SecurityUtil.get().getUser();
        model.addAttribute("budget", budget );
        List<Kind> kinds = kindRepository.findByTypeAndUserGroup(budget.getKind().getType(), user.getGroup());
        Collections.sort(kinds, Comparator.comparing(o -> o.getName()));
        model.addAttribute("kinds", kinds);
        return "edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, Model model) {
        budgetRepository.deleteById(id);
        return getStatistic(null, null, null, null, null,  model);
    }

    @GetMapping("/dictionaries")
    public String getDictionaries() {
        return "dictionaries";
    }

    @GetMapping("/dictionary/{name}")
    public String getDictionary(@PathVariable("name") String name, Model model) {
        if(name.equalsIgnoreCase("KINDS")) {
            List<Kind> kinds = getKinds();
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
    public String editDicKind(@PathVariable("id") String id, @RequestParam(name="error", defaultValue = "") String error, Model model) {
        if(!error.isEmpty()) {
            model.addAttribute("error", error);
        }
        model.addAttribute("kind", kindRepository.findById(id).get());
        return "editDicKind";
    }

    @GetMapping("/dictionary/kinds/delete/{id}")
    public String deleteDicKind(@PathVariable("id") String id, RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();
        Kind kind = kindRepository.findKindByUserGroupAndId(user.getGroup(), id);
        if (budgetRepository.countByUser_GroupAndKind(user.getGroup(), kind) > 0) {
            rm.addFlashAttribute("error", "Невозможно удалить статью, так как она уже используется");
            return String.format("redirect:/dictionary/kinds/edit/%s", id);
        }

        kindRepository.deleteById(id);
        return "redirect:/dictionary/kinds";
    }

    @PostMapping("/dictionary/kinds/create")
    public String createNewDicKind(@ModelAttribute KindTo kindTo, RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();
        Kind check = kindRepository.findKindByUserGroupAndTypeAndNameIgnoreCase(user.getGroup(), kindTo.getType(), kindTo.getName());
        if(check != null) {
            rm.addFlashAttribute("error", "Создание невозможно! Статья с наименованием '" + kindTo.getName() + "'" +
                    " уже используется в '" + kindTo.getType().getValue() + "'!");
            return "redirect:/dictionary/kinds/create";
        }

        Kind kind = createKindFromKindTo(kindTo);
        kind.setId(kindTo.getId());
        kindRepository.save(kind);
        return "redirect:/dictionary/kinds";
    }

    @PostMapping("/dictionary/kinds/edit")
    public String editNewDicKind(@ModelAttribute KindTo kindTo, RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();
        Kind check = kindRepository.findKindByUserGroupAndTypeAndNameIgnoreCase(user.getGroup(), kindTo.getType(), kindTo.getName());
        if(check != null) {
            rm.addFlashAttribute("error", "Изменение невозможно! Статья с наименованием '" + kindTo.getName() + "'" +
                    " уже используется в '" + kindTo.getType().getValue() + "'!");
            return String.format("redirect:/dictionary/kinds/edit/%s", kindTo.getId());
        }

        Kind kindOld = kindRepository.findKindByUserGroupAndId(user.getGroup(), kindTo.getId());
        Kind kind = createKindFromKindTo(kindTo);
        kind.setId(kindTo.getId());
        kind = kindRepository.save(kind);
        List<Budget> budgets = budgetRepository.getBudgetBykindAndUser_Group(kindOld, user.getGroup());
        for(Budget budget : budgets) {
            budget.setKind(kind);
            budgetRepository.save(budget);
        }
        return "redirect:/dictionary/kinds";
    }

    @PostMapping("/")
    public String createNewBudgetItem(@Valid @ModelAttribute BudgetTo budgetTo) {
        Budget budget = createBudgetFromBudgetTo(budgetTo);
        budget.setId(budgetTo.getId());
        budgetRepository.save(budget);
        return "redirect:/statistic";
    }

    public Budget createBudgetFromBudgetTo(BudgetTo b) {
        User user = SecurityUtil.get().getUser();
        Kind kind = kindRepository.findKindByUserGroupAndId(user.getGroup(), b.getKindId());
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

    private List<Kind> getKinds() {
        User user = SecurityUtil.get().getUser();
        return kindRepository.findByUserGroupOrderByTypeAscNameAsc(user.getGroup());
    }


    private Model getBalanceByKind(Model model, Kind kind) {
        User user = SecurityUtil.get().getUser();
        return getBalanceParts(model, budgetRepository.getBudgetBykindAndUser_Group(kind, user.getGroup()));

    }

    private Model getBalanceByDate(Model model, LocalDate date) {
        User user = SecurityUtil.get().getUser();
        return getBalanceParts(model, budgetRepository.getBudgetByDateAndUser_Group(setTimeZoneOffset(date), user.getGroup()));
    }

    private Model getBalanceParts(Model model, List<Budget> budgets) {
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

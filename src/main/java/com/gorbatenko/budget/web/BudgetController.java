package com.gorbatenko.budget.web;


import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.to.BudgetTo;
import com.gorbatenko.budget.web.charts.ChartType;
import com.gorbatenko.budget.util.BaseUtil;
import com.gorbatenko.budget.util.ChartUtil;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.BaseUtil.listBudgetToTreeMap;
import static com.gorbatenko.budget.util.BaseUtil.setTimeZoneOffset;
import static com.gorbatenko.budget.util.SecurityUtil.hidePassword;


@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/budget/")
public class BudgetController extends AbstractWebController {

    @PostMapping("/")
    public String createNewBudgetItem(@Valid @ModelAttribute BudgetTo budgetTo) {
        Budget budget = createBudgetFromBudgetTo(budgetTo);
        budget.setId(budgetTo.getId());
        budgetRepository.save(budget);
        return "redirect:/budget/statistic";
    }

    public Budget createBudgetFromBudgetTo(BudgetTo b) {
        User user = SecurityUtil.get().getUser();
        Kind kind = kindRepository.findKindByUserGroupAndId(user.getGroup(), b.getKindId());
        Budget budget = new Budget(user, kind, LocalDateTime.of(b.getDate(), LocalTime.MIN), b.getDescription(), b.getPrice());
        return budget;
    }

    @GetMapping("/groupstatistic")
    public String getGroupStatistic(@RequestParam(value = "startDate", required=false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate startDate,
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

        model.addAttribute("circleChartProfit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.PROFIT, mapKindSort));
        model.addAttribute("circleChartSpendit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.SPENDING, mapKindSort));
        model.addAttribute("horizontChartProfit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.PROFIT, mapKindSort));
        model.addAttribute("horizontChartSpendit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.SPENDING, mapKindSort));

        return "budget/groupstatistic";
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

        if(typeStr != null) {
            if (!("allTypes".equals(typeStr))) {
                type = Type.valueOf(typeStr);
                listBudget = listBudget.stream().
                        filter(budget -> budget.getKind().getType().equals(type)).
                        collect(Collectors.toList());
                model.addAttribute("typeName", type.getValue());
            }
        }

        if((comment != null) && (!comment.isEmpty())) {
            listBudget = listBudget.stream()
                    .filter(budget -> budget.getDescription().toUpperCase().contains(comment.toUpperCase()))
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

        return "budget/statistic";
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

        return "/budget/create";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        Budget budget = budgetRepository.findById(id).get();
        User user = SecurityUtil.get().getUser();
        model.addAttribute("budget", budget );
        List<Kind> kinds = kindRepository.findByTypeAndUserGroup(budget.getKind().getType(), user.getGroup());
        Collections.sort(kinds, Comparator.comparing(o -> o.getName()));
        model.addAttribute("kinds", kinds);
        return "/budget/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, Model model) {
        budgetRepository.deleteById(id);
        return getStatistic(null, null, "-1", "allTypes", "",  model);
    }



}

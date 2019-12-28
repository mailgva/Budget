package com.gorbatenko.budget.web;


import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Currency;
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
        Currency currency = currencyRepository.findByUserGroupAndId(user.getGroup(), b.getCurrencyId());
        Budget budget = new Budget(user, kind, LocalDateTime.of(b.getDate(), LocalTime.MIN), b.getDescription(),
                b.getPrice(), currency);
        return budget;
    }

    @GetMapping("/groupstatistic")
    public String getGroupStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                    @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                    Model model) {
        User user = SecurityUtil.get().getUser();

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        LocalDate now = LocalDate.now();

        if (startDate == null) {
            offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), 1), LocalTime.MIN);
            startDate = offSetStartDate.toLocalDate();
        }
        offSetStartDate = setTimeZoneOffset(startDate).minusDays(1);

        if (endDate == null) {
            offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth()), LocalTime.MAX);
            endDate = offSetEndDate.toLocalDate();
        }
        offSetEndDate = setTimeZoneOffset(endDate).plusDays(1);

        List<Budget> listBudget = hidePassword(
                filterBudgetByUserCurrencyDefault(budgetRepository.getBudgetByDateBetweenAndUser_Group(
                    offSetStartDate, offSetEndDate, user.getGroup())));

        model = getBalanceParts(model, listBudget);

        Map<Type, Map<Kind, Double>> mapKind = listBudget.stream()
                .collect(Collectors.groupingBy(
                        budget ->
                                budget.getKind().getType(), (
                                Collectors.groupingBy(
                                        Budget::getKind,
                                        TreeMap::new,
                                        Collectors.summingDouble(Budget::getPrice)))));

        TreeMap<Type, Map<Kind, Double>> mapKindSort = new TreeMap<>();
        mapKindSort.putAll(mapKind);

        Map<Kind, Long> mapKindCount = listBudget.stream()
                .collect(Collectors.groupingBy(Budget::getKind, Collectors.counting()));

        model.addAttribute("startDate", BaseUtil.dateToStr(startDate));
        model.addAttribute("endDate", BaseUtil.dateToStr(endDate));
        model.addAttribute("mapKindCount", mapKindCount);
        model.addAttribute("mapKind", mapKindSort);

        model.addAttribute("circleChartProfit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.PROFIT, mapKindSort));
        model.addAttribute("circleChartSpendit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.SPENDING, mapKindSort));
        model.addAttribute("horizontChartProfit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.PROFIT, mapKindSort));
        model.addAttribute("horizontChartSpendit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.SPENDING, mapKindSort));

        return "budget/groupstatistic";
    }

    @GetMapping("/statistic")
    public String getStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                               @RequestParam(value = "kindId", defaultValue = "-1") String id,
                               @RequestParam(value = "type", defaultValue = "allTypes") String typeStr,
                               @RequestParam(value = "comment", defaultValue = "") String comment, Model model) {

        User user = SecurityUtil.get().getUser();

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        LocalDate now = LocalDate.now();

        if (startDate == null) {
            offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), 1), LocalTime.MIN);
            startDate = offSetStartDate.toLocalDate();
        }
        offSetStartDate = setTimeZoneOffset(startDate).minusDays(1);

        if (endDate == null) {
            offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth()), LocalTime.MAX);
            endDate = offSetEndDate.toLocalDate();
        }
        offSetEndDate = setTimeZoneOffset(endDate).plusDays(1);

        Kind kind = new Kind();

        Type type;

        List<Budget> listBudget;


        if ("-1".equals(id)) {
            listBudget = hidePassword(
                    filterBudgetByUserCurrencyDefault(budgetRepository.getBudgetByDateBetweenAndUser_Group(
                        offSetStartDate, offSetEndDate, user.getGroup())));
        } else {
            kind = kindRepository.findKindByUserGroupAndId(user.getGroup(), id);
            listBudget = hidePassword(
                    filterBudgetByUserCurrencyDefault(
                            budgetRepository.getBudgetByKindAndDateBetweenAndUser_Group(kind,
                            offSetStartDate, offSetEndDate, user.getGroup())));
        }

        if (typeStr != null) {
            if (!("allTypes".equals(typeStr))) {
                type = Type.valueOf(typeStr);
                listBudget = listBudget.stream().
                        filter(budget -> budget.getKind().getType().equals(type)).
                        collect(Collectors.toList());
                model.addAttribute("typeName", type.getValue());
            }
        }

        if ((comment != null) && (!comment.isEmpty())) {
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

    @GetMapping("/dynamicstatistic")
    public String getDynamicStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                      @RequestParam(value = "kindId", defaultValue = "") String id,
                                      Model model) {

        if ((startDate == null) || (endDate == null) || (id.isEmpty())) {
            return "redirect:budget/groupstatistic";
        }

        User user = SecurityUtil.get().getUser();

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        offSetStartDate = setTimeZoneOffset(startDate).minusDays(1);
        offSetEndDate = setTimeZoneOffset(endDate).plusDays(1);


        Kind kind = kindRepository.findKindByUserGroupAndId(user.getGroup(), id);
        List<Budget> listBudget = hidePassword(
                filterBudgetByUserCurrencyDefault(budgetRepository.getBudgetByKindAndDateBetweenAndUser_Group(kind,
                        offSetStartDate, offSetEndDate, user.getGroup())));

        Map<String, Double> mapKind = new HashMap<>();

        boolean isInMonth = ((endDate.getYear() == startDate.getYear()) &&
                (endDate.getMonth().equals(startDate.getMonth())));

        mapKind = listBudget.stream()
                .collect(Collectors.groupingBy(
                        (isInMonth ? Budget::getStrDate : Budget::getStrYearMonth),
                        Collectors.summingDouble(Budget::getPrice)));

        TreeMap<String, Double> mapKindSort = new TreeMap<>(mapKind);

        model.addAttribute("kindName", kind.getName());
        model.addAttribute("kindSum", listBudget.stream().mapToDouble(Budget::getPrice).sum());
        model.addAttribute("barChart", ChartUtil.createDynamicMdbChart(ChartType.BARCHART, kind.getName(), mapKindSort));


        return "budget/dynamicstatistic";
    }

    @GetMapping("/create/{type}")
    public String create(@PathVariable("type") String typeStr, Model model) {
        User user = SecurityUtil.get().getUser();
        Type type = Type.valueOf(typeStr.toUpperCase());

        List<Kind> kinds = kindRepository.findByTypeAndUserGroup(type, user.getGroup());

        if (kinds.size() == 0) {
            return "/dictionaries/kinds/create";
        }

        kinds.sort(Comparator.comparing(Kind::getName));

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;
        LocalDate now = LocalDate.now();

        offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), 1), LocalTime.MIN);
        offSetStartDate = setTimeZoneOffset(offSetStartDate.toLocalDate()).minusDays(1);

        offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth()), LocalTime.MAX);
        offSetEndDate = setTimeZoneOffset(offSetEndDate.toLocalDate()).plusDays(1);

        kinds = sortKindsByPopular(kinds, type, offSetStartDate, offSetEndDate, user.getGroup());

        List<Currency> currencies = currencyRepository.findByUserGroupOrderByNameAsc(user.getGroup());

        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        model.addAttribute("kinds", kinds);
        model.addAttribute("type", type);
        model.addAttribute("date", localDate.format(formatter));
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", user.getCurrencyDefault());
        return "/budget/create";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        Budget budget = budgetRepository.findById(id).get();
        User user = SecurityUtil.get().getUser();
        Type type = budget.getKind().getType();

        List<Kind> kinds = kindRepository.findByTypeAndUserGroup(type, user.getGroup());
        kinds.sort(Comparator.comparing(Kind::getName));

        List<Currency> currencies = currencyRepository.findByUserGroupOrderByNameAsc(user.getGroup());

        model.addAttribute("budget", budget);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", user.getCurrencyDefault());
        return "/budget/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, Model model) {
        budgetRepository.deleteById(id);
        return getStatistic(null, null, "-1", "allTypes", "", model);
    }


}

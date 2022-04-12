package com.gorbatenko.budget.web;


import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.to.BudgetTo;
import com.gorbatenko.budget.util.ChartUtil;
import com.gorbatenko.budget.util.SecurityUtil;
import com.gorbatenko.budget.util.TypePeriod;
import com.gorbatenko.budget.web.charts.ChartType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.BaseUtil.*;
import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;
import static java.util.stream.Collectors.groupingBy;


@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/budget/")
public class BudgetController extends AbstractWebController {

    @PostMapping("/")
    public String createBudget(@Valid @ModelAttribute BudgetTo budgetTo, HttpServletRequest request) {
        String formatLink = "redirect:/budget/statistic?startDate=%s&endDate=%s#d_%s";
        if (budgetTo.getId().isEmpty()) {
            budgetTo.setId(null);
        }

        int sumTimezoneOffsetMinutes = getSumTimezoneOffsetMinutes(request);

        Budget budget = createBudgetFromBudgetTo(budgetTo);
        budget.setCreateDateTime(LocalDateTime.now().plusMinutes(sumTimezoneOffsetMinutes));
        budget.setId(budgetTo.getId());
        budgetRepository.save(budget);

        LocalDate date = budget.getDate().toLocalDate();

        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate endDate = LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth());

        // if day is in current month
        if (startDate.minusDays(1).isBefore(date) && (endDate.plusDays(1).isAfter(date))) {
            return String.format(formatLink, dateToStr(startDate), dateToStr(endDate), dateToStr(date));
        } else {
            startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
            endDate = LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth());
            return String.format(formatLink, dateToStr(startDate), dateToStr(endDate), dateToStr(date));
        }
    }

    private Budget createBudgetFromBudgetTo(BudgetTo b) {
        Kind kind = kindRepository.getById(b.getKindId());
        Currency currency = currencyRepository.getById(b.getCurrencyId());
        return new Budget(toDocUser(SecurityUtil.get().getUser()),
                kind,
                LocalDateTime.of(b.getDate(), LocalTime.MIN),
                b.getDescription(),
                b.getPrice(),
                currency);
    }

    @GetMapping("/groupstatistic")
    public String getGroupStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                    @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                    @RequestParam(value = "period", required = false, defaultValue = "") TypePeriod period,
                                    @RequestParam(value = "sortType", required = false, defaultValue = "") String sortType,
                                    Model model) {

        if(period == null) {
            period = TypePeriod.SELECTED_PERIOD;
        }

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        LocalDate now = LocalDate.now();

        if ((startDate == null) || (period.equals(TypePeriod.CURRENT_MONTH))) {
            offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), 1), LocalTime.MIN);
            startDate = offSetStartDate.toLocalDate();
            offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth()), LocalTime.MAX);
            endDate = offSetEndDate.toLocalDate();
        }

        if (period.equals(TypePeriod.CURRENT_YEAR)) {
            offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), 1, 1), LocalTime.MIN);
            startDate = offSetStartDate.toLocalDate();
            offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), 12, 31), LocalTime.MAX);
            endDate = offSetEndDate.toLocalDate();
        }

        offSetStartDate = setTimeZoneOffset(startDate);
        offSetEndDate = setTimeZoneOffset(endDate);

        List<Budget> listBudget = budgetRepository.getFilteredData(offSetStartDate, offSetEndDate, null, null, null, null, null, period);

        if (period.equals(TypePeriod.ALL_TIME) && listBudget.size() > 0) {
            offSetStartDate = listBudget.stream().min(Comparator.comparing(Budget::getDate)).get().getDate();
        }

        Map<Type, Map<Kind, Double>> mapKind;
        if (sortType.isEmpty() || sortType.equalsIgnoreCase("byName")) {
            mapKind = listBudget.stream()
                    .collect(groupingBy(
                            budget ->
                                    budget.getKind().getType(), (
                                    groupingBy(
                                            Budget::getKind,
                                            TreeMap::new,
                                            Collectors.summingDouble(Budget::getPrice)))
                    ));
        } else {
            mapKind = listBudget.stream()
                    .collect(groupingBy(
                            budget ->
                                    budget.getKind().getType(), (
                                    groupingBy(
                                            Budget::getKind,
                                            HashMap::new,
                                            Collectors.summingDouble(Budget::getPrice))))
                    );

            for (Map.Entry<Type, Map<Kind, Double>> entry : mapKind.entrySet()) {
                entry.setValue(
                        entry.getValue().entrySet()
                                .stream()
                                .sorted((s1,s2)->s2.getValue().compareTo(s1.getValue()) /*Map.Entry.comparingByValue()*/)
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
            }
        }

        if (period.equals(TypePeriod.ALL_TIME)) {
            startDate = listBudget.stream().
                    map(Budget::getDate).
                    map(d -> LocalDate.of(d.getYear(), d.getMonth(), d.getDayOfMonth())).
                    min(LocalDate::compareTo).get();

            endDate = listBudget.stream().
                    map(Budget::getDate).
                    map(d -> LocalDate.of(d.getYear(), d.getMonth(), d.getDayOfMonth())).
                    max(LocalDate::compareTo).get();
        }

        boolean isInMonth = ((endDate.getYear() == startDate.getYear()) &&
                (endDate.getMonth().equals(startDate.getMonth())));

        Map<String, Double> mapDateProfit = listBudget.stream()
                .filter(budget -> budget.getKind().getType().equals(Type.PROFIT))
                .collect(groupingBy(
                        (isInMonth ?  Budget::getStrDate : Budget::getStrYearMonth),
                        Collectors.summingDouble(Budget::getPrice)));

        Map<String, Double> mapDateSpending = listBudget.stream()
                .filter(budget -> budget.getKind().getType().equals(Type.SPENDING))
                .collect(groupingBy(
                        (isInMonth ?  Budget::getStrDate : Budget::getStrYearMonth),
                        Collectors.summingDouble(Budget::getPrice)));

        TreeMap<String, TreeMap<Type, Double>> totalMap = new TreeMap<>();

        for(Map.Entry<String, Double> entry : mapDateProfit.entrySet()) {
            TreeMap<Type, Double> map = totalMap.getOrDefault(entry.getKey(), new TreeMap<>());
            double value = map.getOrDefault(Type.PROFIT, 0.00D) + entry.getValue();
            map.put(Type.PROFIT, value);
            totalMap.put(entry.getKey(), map);
        }

        for(Map.Entry<String, Double> entry : mapDateSpending.entrySet()) {
            TreeMap<Type, Double> map = totalMap.getOrDefault(entry.getKey(), new TreeMap<>());
            double value = map.getOrDefault(Type.SPENDING, 0.00D) + entry.getValue();
            map.put(Type.SPENDING, value);
            totalMap.put(entry.getKey(), map);
        }

        TreeMap<Type, Map<Kind, Double>> mapKindSort = new TreeMap<>(mapKind);

        Map<Kind, Long> mapKindCount = listBudget.stream()
                .collect(groupingBy(Budget::getKind, Collectors.counting()));

        Double maxPriceProfit;
        Double maxPriceSpending;

        maxPriceProfit = listBudget.stream()
                .filter(budget -> budget.getKind().getType().equals(Type.PROFIT))
                .collect(groupingBy(Budget::getKind, Collectors.summingDouble(Budget::getPrice)))
                .values().stream()
                .max(Double::compareTo)
                .orElse(0.0);

        maxPriceSpending = listBudget.stream()
                .filter(budget -> budget.getKind().getType().equals(Type.SPENDING))
                .collect(groupingBy(Budget::getKind, Collectors.summingDouble(Budget::getPrice)))
                .values().stream()
                .max(Double::compareTo)
                .orElse(0.0);

        Map<Type, Double> mapMaxPrice = new HashMap<>();
        mapMaxPrice.put(Type.PROFIT, maxPriceProfit);
        mapMaxPrice.put(Type.SPENDING, maxPriceSpending);

        getBalanceParts(model, listBudget, offSetStartDate.minusDays(1), offSetEndDate);
        model.addAttribute("startDate", dateToStr(startDate));
        model.addAttribute("endDate", dateToStr(endDate));
        model.addAttribute("mapKindCount", mapKindCount);
        model.addAttribute("mapKind", mapKindSort);
        model.addAttribute("mapMaxPrice", mapMaxPrice);

        model.addAttribute("circleChartProfit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.PROFIT, mapKindSort));
        model.addAttribute("circleChartSpendit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.SPENDING, mapKindSort));
        model.addAttribute("horizontChartProfit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.PROFIT, mapKindSort));
        model.addAttribute("horizontChartSpendit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.SPENDING, mapKindSort));

        model.addAttribute("totalBarChart", ChartUtil.createDynamicMultiMdbChart(ChartType.BARCHART, totalMap));

        model.addAttribute("period", period);
        model.addAttribute("sortType", sortType);

        model.addAttribute("pageName", "Групповая статистика");

        return "budget/groupstatistic";
    }

    @GetMapping("/statistic")
    public String getStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                               @RequestParam(value = "userId", defaultValue = "-1") String userId,
                               @RequestParam(value = "kindId", defaultValue = "-1") String kindId,
                               @RequestParam(value = "type", defaultValue = "allTypes") String typeStr,
                               @RequestParam(value = "price", defaultValue = "") String priceStr,
                               @RequestParam(value = "description", defaultValue = "") String description,
                               @RequestParam(value = "period", required = false, defaultValue = "") TypePeriod period,
                               Model model) {

        if(period == null) {
            period = TypePeriod.SELECTED_PERIOD;
        }

        List<com.gorbatenko.budget.model.doc.User> users = new HashSet<>(budgetRepository.getUsersForAllPeriod())
                .stream()
                .sorted(Comparator.comparing(com.gorbatenko.budget.model.doc.User::getName))
                .collect(Collectors.toList());

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        LocalDate now = LocalDate.now();

        if (startDate == null) {
            offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), 1), LocalTime.MIN);
            startDate = offSetStartDate.toLocalDate();
        }
        offSetStartDate = setTimeZoneOffset(startDate);

        if (endDate == null) {
            offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth()), LocalTime.MAX);
            endDate = offSetEndDate.toLocalDate();
        }
        offSetEndDate = setTimeZoneOffset(endDate);

        List<Budget> listBudget = budgetRepository.getFilteredData(offSetStartDate, offSetEndDate, userId, typeStr, kindId, priceStr, description, period);

        if ((typeStr != null) && (!("allTypes".equals(typeStr)))) {
            model.addAttribute("typeName", Type.valueOf(typeStr).getValue());
        }

        getBalanceParts(model, listBudget, offSetStartDate.minusDays(1), offSetEndDate);
        TreeMap<LocalDate, List<Budget>> map = listBudgetToTreeMap(listBudget);
        model.addAttribute("startDate", dateToStr(startDate));
        model.addAttribute("endDate", dateToStr(endDate));
        model.addAttribute("listBudget", map);
        model.addAttribute("users", users);
        model.addAttribute("userId", userId);
        model.addAttribute("kindList", getKinds());
        model.addAttribute("kindId", kindId);
        model.addAttribute("description", description);
        model.addAttribute("price", priceStr);

        model.addAttribute("pageName", "Статистика");

        return "budget/statistic";
    }

    @GetMapping("/dynamicstatistic")
    public String getDynamicStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                      @RequestParam(value = "kindId", defaultValue = "") String kindId,
                                      @RequestParam(value = "type", required = false, defaultValue = "") Type type,
                                      Model model) {

        if ((startDate == null) || (endDate == null)) {
            return "redirect:budget/groupstatistic";
        }

        if ((kindId.isEmpty() && type == null)) {
            return "redirect:budget/groupstatistic";
        }

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        offSetStartDate = setTimeZoneOffset(startDate).minusDays(1);
        offSetEndDate = setTimeZoneOffset(endDate).plusDays(1);

        String positionName;
        double positionSum;

        List<Budget> listBudget;

        boolean isInMonth = ((endDate.getYear() == startDate.getYear()) &&
                (endDate.getMonth().equals(startDate.getMonth())));


        if(! kindId.isEmpty()) {
            Kind kind = kindRepository.getById(kindId);

            listBudget =
                    budgetRepository.getFilteredData(offSetStartDate, offSetEndDate, null, null, kind.getId(), null, null, TypePeriod.SELECTED_PERIOD);

            positionName = kind.getName();
        } else {
            listBudget =
                    budgetRepository.getFilteredData(offSetStartDate, offSetEndDate, null, type.name(), null, null, null, TypePeriod.SELECTED_PERIOD);

            positionName = type.getValue();
        }

        Map<String, Double> mapKind = listBudget.stream()
                .collect(groupingBy(
                        (isInMonth ? Budget::getStrDate : Budget::getStrYearMonth),
                        Collectors.summingDouble(Budget::getPrice)));

        TreeMap<String, Double> mapKindSort = new TreeMap<>(mapKind);

        positionSum = listBudget.stream()
                .mapToDouble(Budget::getPrice).sum();

        model.addAttribute("positionName", positionName);
        model.addAttribute("positionSum", positionSum);
        model.addAttribute("barChart", ChartUtil.createDynamicMdbChart(ChartType.BARCHART, positionName, mapKindSort));

        model.addAttribute("pageName", "Динамика");

        return "budget/dynamicstatistic";
    }

    @GetMapping("/create/{type}")
    public String create(@PathVariable("type") String typeStr, Model model,
                         HttpServletRequest request) {
        int sumTimezoneOffsetMinutes = getSumTimezoneOffsetMinutes(request);

        Budget budget = new Budget();
        Type type = Type.valueOf(typeStr.toUpperCase());
        List<Kind> kinds = kindRepository.getFilteredData(null,null, type);

        if (kinds.size() == 0) {
            return "redirect:/dictionaries/kinds/create/"+typeStr;
        }

        kinds.sort(Comparator.comparing(Kind::getName));

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;
        LocalDate now = LocalDate.now();

        offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), 1), LocalTime.MIN);
        offSetStartDate = setTimeZoneOffset(offSetStartDate.toLocalDate()).minusDays(1);

        offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth()), LocalTime.MAX);
        offSetEndDate = setTimeZoneOffset(offSetEndDate.toLocalDate()).plusDays(1);

        kinds = sortKindsByPopular(kinds, type, offSetStartDate, offSetEndDate);

        budget.setKind(kinds.get(0));

        String kindId = (model.asMap().containsKey("kindId") ? (String) model.asMap().get("kindId") : "");

        if(!kindId.isEmpty()) {
            budget.setKind(kinds.stream()
                    .filter(k -> k.getId().equals(kindId)).findFirst().get());
        }

        budget.setDate(LocalDateTime.of(
                LocalDateTime.now().plusMinutes(sumTimezoneOffsetMinutes).toLocalDate(),
                LocalTime.of(0,0)));
        budget.setCurrency(getCurrencyDefault());

        List<Currency> currencies = currencyRepository.getFilteredData(null, null);

        model.addAttribute("budget", budget);
        model.addAttribute("type", type);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", getCurrencyDefault());

        model.addAttribute("pageName", "Создание");

        return "/budget/edit";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        Budget budget = budgetRepository.getById(id);
        Type type = budget.getKind().getType();

        List<Kind> kinds = kindRepository.getFilteredData(null, null, type);
        kinds.sort(Comparator.comparing(Kind::getName));

        List<Currency> currencies = currencyRepository.getFilteredData(null, null);

        String kindId = (model.asMap().containsKey("kindId") ? (String) model.asMap().get("kindId") : "");

        if(!kindId.isEmpty()) {
            budget.setKind(kinds.stream()
                    .filter(k -> k.getId().equals(kindId)).findFirst().get());
        }

        model.addAttribute("budget", budget);
        model.addAttribute("kinds", kinds);
        model.addAttribute("type", type);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", getCurrencyDefault());

        model.addAttribute("pageName", "Изменение");

        return "/budget/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, Model model) {
        budgetRepository.deleteById(id);
        return "redirect:/budget/statistic";
    }


    public static int getSumTimezoneOffsetMinutes(HttpServletRequest request) {
        int userTimezoneOffsetMinutes = getUserTimezoneOffsetMinutes(request);

        int currentTimezomeOffsetMinutes = OffsetDateTime.now().getOffset().get(ChronoField.OFFSET_SECONDS) / 60;

        return (userTimezoneOffsetMinutes + currentTimezomeOffsetMinutes) * -1;
    }

    private static int getUserTimezoneOffsetMinutes(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();

        String cookieName = "userTimezoneOffset";

        int defaultValue = 1;

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()))
                return (Integer.parseInt(cookie.getValue()));
        }
        return(defaultValue);
    }

    private com.gorbatenko.budget.model.doc.User toDocUser(com.gorbatenko.budget.model.User user) {
        return new com.gorbatenko.budget.model.doc.User(user.getId(), user.getName());
    }
}

package com.gorbatenko.budget.web;


import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.*;
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
import java.time.*;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.BaseUtil.*;
import static com.gorbatenko.budget.util.SecurityUtil.hidePassword;
import static java.util.stream.Collectors.groupingBy;


@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/budget/")
public class BudgetController extends AbstractWebController {

    @PostMapping("/")
    public String createNewBudgetItem(@Valid @ModelAttribute BudgetTo budgetTo, HttpServletRequest request) {
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
        User user = SecurityUtil.get().getUser();
        Kind kind = kindRepository.getKindByUserGroupAndId(user.getGroup(), b.getKindId());
        Currency currency = currencyRepository.getCurrencyByUserGroupAndId(user.getGroup(), b.getCurrencyId());
        Budget budget = new Budget(user, kind, LocalDateTime.of(b.getDate(), LocalTime.MIN), b.getDescription(),
                b.getPrice(), currency);
        return budget;
    }

    @GetMapping("/groupstatistic")
    public String getGroupStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                    @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                    @RequestParam(value = "period", required = false, defaultValue = "") TypePeriod period,
                                    @RequestParam(value = "sorttype", required = false, defaultValue = "") String sorttype,
                                    Model model) {

        if(period == null) {
            period = TypePeriod.SELPERIOD;
        }

        User user = SecurityUtil.get().getUser();

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        LocalDate now = LocalDate.now();

        if ((startDate == null) || (period.equals(TypePeriod.CURRENTMONTH))) {
            offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), 1), LocalTime.MIN);
            startDate = offSetStartDate.toLocalDate();
        }
        if (period.equals(TypePeriod.CURRENTYEAR)) {
            offSetStartDate = LocalDateTime.of(LocalDate.of(now.getYear(), 1, 1), LocalTime.MIN);
            startDate = offSetStartDate.toLocalDate();
        }

        offSetStartDate = setTimeZoneOffset(startDate).minusDays(1);

        if ((endDate == null) || (period.equals(TypePeriod.CURRENTMONTH))) {
            offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth()), LocalTime.MAX);
            endDate = offSetEndDate.toLocalDate();
        }
        if (period.equals(TypePeriod.CURRENTYEAR)) {
            offSetEndDate = LocalDateTime.of(LocalDate.of(now.getYear(), 12, 31), LocalTime.MAX);
            endDate = offSetEndDate.toLocalDate();
        }

        offSetEndDate = setTimeZoneOffset(endDate).plusDays(1);

        List<Budget> listBudget = hidePassword(
                filterBudgetByUserCurrencyDefault(
                        period.equals(TypePeriod.ALLTIME) ?
                                budgetRepository.getAllByUser_Group(user.getGroup()) :
                                budgetRepository.getBudgetByDateBetweenAndUser_Group(
                                        offSetStartDate, offSetEndDate, user.getGroup())));

        model = getBalanceParts(model, listBudget);

        Map<Type, Map<Kind, Double>> mapKind;
        if (sorttype.isEmpty() || sorttype.equalsIgnoreCase("sortbyname")) {
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
                        (Map<Kind, Double>) entry.getValue().entrySet()
                        .stream()
                                .sorted((s1,s2)->s2.getValue().compareTo(s1.getValue()) /*Map.Entry.comparingByValue()*/)
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));

            }

        }

        if (period.equals(TypePeriod.ALLTIME)) {
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

        TreeMap<Type, Map<Kind, Double>> mapKindSort = new TreeMap<>();
        mapKindSort.putAll(mapKind);

        Map<Kind, Long> mapKindCount = listBudget.stream()
                .collect(groupingBy(Budget::getKind, Collectors.counting()));


        Double maxPriceProfit = 0.0d;
        Double maxPriceSpending = 0.0d;

        maxPriceProfit = listBudget.stream()
                .filter(budget -> budget.getKind().getType().equals(Type.PROFIT))
                .collect(groupingBy(Budget::getKind, Collectors.summingDouble(Budget::getPrice)))
                .entrySet().stream()
                .map(Map.Entry::getValue)
                .max(Double::compareTo)
                .orElse(0.0);

        maxPriceSpending = listBudget.stream()
                .filter(budget -> budget.getKind().getType().equals(Type.SPENDING))
                .collect(groupingBy(Budget::getKind, Collectors.summingDouble(Budget::getPrice)))
                .entrySet().stream()
                .map(Map.Entry::getValue)
                .max(Double::compareTo)
                .orElse(0.0);

        Map<Type, Double> mapMaxPrice = new HashMap<>();
        mapMaxPrice.put(Type.PROFIT, maxPriceProfit);
        mapMaxPrice.put(Type.SPENDING, maxPriceSpending);

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
        model.addAttribute("sorttype", sorttype);

        model.addAttribute("pageName", "Групповая статистика");

        return "budget/groupstatistic";
    }

    @GetMapping("/statistic")
    public String getStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                               @RequestParam(value = "userId", defaultValue = "-1") String userId,
                               @RequestParam(value = "kindId", defaultValue = "-1") String id,
                               @RequestParam(value = "type", defaultValue = "allTypes") String typeStr,
                               @RequestParam(value = "price", defaultValue = "") String priceStr,
                               @RequestParam(value = "comment", defaultValue = "") String comment,
                               @RequestParam(value = "alltime", required = false, defaultValue = "") String allTime,
                               Model model) {

        User user = SecurityUtil.get().getUser();

        List<User> users = budgetRepository.getAllByUser_Group(user.getGroup()).stream()
                .map(Budget::getUser)
                .collect(Collectors.toSet())
                .stream()
                .sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toList());

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

        List<Budget> listBudget;


        if ("-1".equals(id)) {
            listBudget = hidePassword(
                    filterBudgetByUserCurrencyDefault(
                            (allTime.equalsIgnoreCase("YES") ?
                             budgetRepository.getAllByUser_Group(user.getGroup()) :
                             budgetRepository.getBudgetByDateBetweenAndUser_Group(offSetStartDate, offSetEndDate, user.getGroup())
                             )
                    ));
        } else {
            kind = kindRepository.getKindByUserGroupAndId(user.getGroup(), id);
            listBudget = hidePassword(
                    filterBudgetByUserCurrencyDefault(
                            (allTime.equalsIgnoreCase("YES") ?
                             budgetRepository.getBudgetByKindAndUser_Group(kind, user.getGroup()) :
                             budgetRepository.getBudgetByKindAndDateBetweenAndUser_Group(kind,offSetStartDate, offSetEndDate, user.getGroup())
                            )

                    ));
        }

        if(allTime.equalsIgnoreCase("YES")) {
            startDate = listBudget.stream()
                    .map(budget -> budget.getDate().toLocalDate())
                    .min(LocalDate::compareTo)
                    .get();

            endDate = listBudget.stream()
                    .map(budget -> budget.getDate().toLocalDate())
                    .max(LocalDate::compareTo)
                    .get();
        }

        if ((typeStr != null) && (!("allTypes".equals(typeStr)))) {
                model.addAttribute("typeName", Type.valueOf(typeStr).getValue());
        }

        listBudget = listBudget.stream()
                .filter(budget -> ("allTypes".equals(typeStr) || (budget.getKind().getType().equals(Type.valueOf(typeStr)))))
                .filter(budget -> ((userId == null) || (userId.isEmpty()) || (userId.equals("-1")) || (budget.getUser().getId().equals(userId))))
                .filter(budget -> ((comment == null) || (comment.isEmpty()) || (budget.getDescription().toUpperCase().contains(comment.toUpperCase()))))
                .filter(budget -> ((priceStr == null) || (priceStr.isEmpty()) || (budget.getPrice() == Double.parseDouble(priceStr))))
                .collect(Collectors.toList());

        model = getBalanceParts(model, listBudget);
        TreeMap<LocalDate, List<Budget>> map = listBudgetToTreeMap(listBudget);
        model.addAttribute("startDate", dateToStr(startDate));
        model.addAttribute("endDate", dateToStr(endDate));
        model.addAttribute("listBudget", map);
        model.addAttribute("users", users);
        model.addAttribute("userId", userId);
        model.addAttribute("kindList", getKinds());
        model.addAttribute("kindName", kind.getName());
        model.addAttribute("comment", comment);
        model.addAttribute("price", priceStr);

        model.addAttribute("pageName", "Статистика");

        return "budget/statistic";
    }

    @GetMapping("/dynamicstatistic")
    public String getDynamicStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                      @RequestParam(value = "kindId", defaultValue = "") String id,
                                      @RequestParam(value = "type", required = false, defaultValue = "") Type type,
                                      Model model) {


        if ((startDate == null) || (endDate == null)) {
            return "redirect:budget/groupstatistic";
        }

        if ((id.isEmpty() && type == null)) {
            return "redirect:budget/groupstatistic";
        }

        User user = SecurityUtil.get().getUser();

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        offSetStartDate = setTimeZoneOffset(startDate).minusDays(1);
        offSetEndDate = setTimeZoneOffset(endDate).plusDays(1);

        String positionName = "";
        double positionSum = 0.0D;

        List<Budget> listBudget;

        boolean isInMonth = ((endDate.getYear() == startDate.getYear()) &&
                (endDate.getMonth().equals(startDate.getMonth())));


        if(! id.isEmpty()) {
            Kind kind = kindRepository.getKindByUserGroupAndId(user.getGroup(), id);

            listBudget = hidePassword(
                    filterBudgetByUserCurrencyDefault(budgetRepository.getBudgetByKindAndDateBetweenAndUser_Group(kind,
                            offSetStartDate, offSetEndDate, user.getGroup())));

            positionName = kind.getName();

        } else {
            positionName = type.getValue();

            listBudget = hidePassword(filterBudgetByUserCurrencyDefault(
                    budgetRepository.getAllByKindTypeAndDateBetweenAndUser_Group(type,
                    offSetStartDate, offSetEndDate, user.getGroup())));

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
        User user = SecurityUtil.get().getUser();

        int sumTimezoneOffsetMinutes = getSumTimezoneOffsetMinutes(request);

        Budget budget = new Budget();
        Type type = Type.valueOf(typeStr.toUpperCase());
        List<Kind> kinds = kindRepository.getKindByTypeAndUserGroup(type, user.getGroup());

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

        kinds = sortKindsByPopular(kinds, type, offSetStartDate, offSetEndDate, user.getGroup());

        budget.setKind(kinds.get(0));

        String kindId = (model.asMap().containsKey("kindId") ? (String) model.asMap().get("kindId") : "");

        if(!kindId.isEmpty()) {
            budget.setKind(kinds.stream()
                    .filter(k -> k.getId().equals(kindId)).findFirst().get());
        }

        budget.setDate(LocalDateTime.of(
                LocalDateTime.now().plusMinutes(sumTimezoneOffsetMinutes).toLocalDate(),
                LocalTime.of(0,0)));
        budget.setCurrency(user.getCurrencyDefault());

        List<Currency> currencies = currencyRepository.getCurrencyByUserGroupOrderByNameAsc(user.getGroup());

        model.addAttribute("budget", budget);
        model.addAttribute("type", type);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", user.getCurrencyDefault());

        model.addAttribute("pageName", "Создание");

        return "/budget/edit";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        User user = SecurityUtil.get().getUser();
        Budget budget = budgetRepository.findById(id).get();
        Type type = budget.getKind().getType();

        List<Kind> kinds = kindRepository.getKindByTypeAndUserGroup(type, user.getGroup());
        kinds.sort(Comparator.comparing(Kind::getName));

        List<Currency> currencies = currencyRepository.getCurrencyByUserGroupOrderByNameAsc(user.getGroup());

        String kindId = (model.asMap().containsKey("kindId") ? (String) model.asMap().get("kindId") : "");

        if(!kindId.isEmpty()) {
            budget.setKind(kinds.stream()
                    .filter(k -> k.getId().equals(kindId)).findFirst().get());
        }

        model.addAttribute("budget", budget);
        model.addAttribute("kinds", kinds);
        model.addAttribute("type", type);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", user.getCurrencyDefault());

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

        for ( int i=0; i<cookies.length; i++) {

            Cookie cookie = cookies[i];

            if (cookieName.equals(cookie.getName()))

                return(Integer.parseInt(cookie.getValue()));

        }

        return(defaultValue);
    }
}

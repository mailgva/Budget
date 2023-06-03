package com.gorbatenko.budget.web;


import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.to.BudgetTo;
import com.gorbatenko.budget.util.*;
import com.gorbatenko.budget.web.charts.ChartType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.BaseUtil.*;
import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;
import static java.util.stream.Collectors.groupingBy;


@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/budget/")
public class BudgetItemController extends AbstractWebController {

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createBudget(@Valid @RequestBody BudgetTo budgetTo, TimeZone tz) {
        String formatLink = "redirect:/budget/statistic?startDate=%s&endDate=%s&currencyId=%s#d_%s";
        if (budgetTo.getId().isEmpty()) {
            budgetTo.setId(null);
        }

        int sumTimezoneOffsetMinutes = getSumTimeZoneOffsetMinutes(tz);

        BudgetItem budgetItem = createBudgetFromBudgetTo(budgetTo);
        budgetItem.setCreateDateTime(LocalDateTime.now().plusMinutes(sumTimezoneOffsetMinutes));
        budgetItem.setId(budgetTo.getId());
        budgetItemRepository.save(budgetItem);

        LocalDate date = budgetItem.getDate().toLocalDate();

        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate endDate = LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth());

        // if day is in current month
        if (!(startDate.minusDays(1).isBefore(date) && (endDate.plusDays(1).isAfter(date)))) {
            startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
            endDate = LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth());
        }
        return String.format(formatLink, dateToStr(startDate), dateToStr(endDate), budgetTo.getCurrencyId(), dateToStr(date));

    }

    private BudgetItem createBudgetFromBudgetTo(BudgetTo b) {
        Kind kind = kindRepository.getById(b.getKindId());
        Currency currency = currencyRepository.getById(b.getCurrencyId());
        return new BudgetItem(toDocUser(SecurityUtil.get().getUser()),
                kind,
                LocalDateTime.of(b.getDate(), LocalTime.MIN),
                b.getDescription(),
                b.getPrice(),
                currency);
    }

    @GetMapping("groupstatistic")
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

        List<KindTotals> totals = budgetItemRepository.getTotalsByKinds(offSetStartDate, offSetEndDate, null, null, null, null, null, period);

        if (period.equals(TypePeriod.ALL_TIME) && totals.size() > 0) {
            offSetStartDate = totals.stream()
                    .map(total -> total.getMinCreateDateTime())
                    .min(LocalDateTime::compareTo)
                    .get();
        }

        TreeMap<Type, Map<Kind, Double>> mapKind;

        if (sortType.isEmpty() || sortType.equalsIgnoreCase("byName")) {
            mapKind = new TreeMap(totals.stream()
                    .collect(groupingBy(
                            total ->
                                    total.getKind().getType(), (
                                    groupingBy(
                                            KindTotals::getKind,
                                            TreeMap::new,
                                            Collectors.summingDouble(KindTotals::getSumPrice)))
                    )));
        } else { /* sort by price */
            mapKind = new TreeMap(totals.stream()
                    .collect(groupingBy(
                            total ->
                                    total.getKind().getType(), (
                                    groupingBy(
                                            KindTotals::getKind,
                                            HashMap::new,
                                            Collectors.summingDouble(KindTotals::getSumPrice))))
                    ));

            for (Map.Entry<Type, Map<Kind, Double>> entry : mapKind.entrySet()) {
                entry.setValue(
                        entry.getValue().entrySet()
                                .stream()
                                .sorted((s1, s2) -> s2.getValue().compareTo(s1.getValue()))
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
            }
        }

        if (period.equals(TypePeriod.ALL_TIME)) {
            startDate = totals.stream()
                    .map(KindTotals::getMinCreateDateTime)
                    .min(LocalDateTime::compareTo)
                    .get()
                    .toLocalDate();

            endDate = totals.stream()
                    .map(KindTotals::getMaxCreateDateTime)
                    .max(LocalDateTime::compareTo)
                    .get()
                    .toLocalDate();
        }

        GroupPeriod groupPeriod = getGroupPeriod(startDate, endDate);

        Map<String, Double> mapDateProfit =
                budgetItemRepository.getSumPriceForPeriodByDateAndDefaultCurrency(offSetStartDate, offSetEndDate, Type.PROFIT, period, groupPeriod);

        Map<String, Double> mapDateSpending =
                budgetItemRepository.getSumPriceForPeriodByDateAndDefaultCurrency(offSetStartDate, offSetEndDate, Type.SPENDING, period, groupPeriod);

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

        Map<Kind, Long> mapKindCount = totals.stream()
                .collect(Collectors.toMap(KindTotals::getKind, KindTotals::getCount));

        Double maxPriceProfit = totals.stream()
                .filter(total -> total.getKind().getType().equals(Type.PROFIT))
                .map(total -> total.getSumPrice())
                .max(Double::compareTo)
                .orElse(0.0);

        Double maxPriceSpending = totals.stream()
                .filter(total -> total.getKind().getType().equals(Type.SPENDING))
                .map(total -> total.getSumPrice())
                .max(Double::compareTo)
                .orElse(0.0);

        Map<Type, Double> mapMaxPrice = new HashMap<>();
        mapMaxPrice.put(Type.PROFIT, maxPriceProfit);
        mapMaxPrice.put(Type.SPENDING, maxPriceSpending);

        Double profit = totals.stream()
                .filter(total -> total.getKind().getType().equals(Type.PROFIT))
                .mapToDouble(KindTotals::getSumPrice)
                .sum();

        Double spending = totals.stream()
                .filter(total -> total.getKind().getType().equals(Type.SPENDING))
                .mapToDouble(KindTotals::getSumPrice)
                .sum();

        Double remain = profit - spending;

        model.addAttribute("profit", profit);
        model.addAttribute("spending", spending);
        model.addAttribute("remain", remain);
        model.addAttribute("remainOnStartPeriod", getRemainOnStartPeriod(offSetStartDate.minusDays(1)));
        model.addAttribute("remainOnEndPeriod", getRemainOnStartPeriod(offSetEndDate));

        model.addAttribute("startDate", dateToStr(startDate));
        model.addAttribute("endDate", dateToStr(endDate));
        model.addAttribute("mapKindCount", mapKindCount);
        model.addAttribute("mapKind", mapKind);
        model.addAttribute("mapMaxPrice", mapMaxPrice);

        model.addAttribute("circleChartProfit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.PROFIT, mapKind));
        model.addAttribute("circleChartSpendit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.SPENDING, mapKind));
        model.addAttribute("horizontChartProfit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.PROFIT, mapKind));
        model.addAttribute("horizontChartSpendit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.SPENDING, mapKind));

        model.addAttribute("totalBarChart", ChartUtil.createDynamicMultiMdbChart(ChartType.BARCHART, totalMap));

        model.addAttribute("period", period);
        model.addAttribute("sortType", sortType);

        model.addAttribute("pageName", "Групповая статистика");
        model.addAttribute("filteredPage", true);

        return "budget/groupstatistic";
    }

    @GetMapping("statistic")
    public String getStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                               @RequestParam(value = "userId", defaultValue = "-1") String userId,
                               @RequestParam(value = "kindId", defaultValue = "-1") String kindId,
                               @RequestParam(value = "type", defaultValue = "allTypes") String typeStr,
                               @RequestParam(value = "price", defaultValue = "") String priceStr,
                               @RequestParam(value = "description", defaultValue = "") String description,
                               @RequestParam(value = "period", required = false, defaultValue = "") TypePeriod period,
                               @RequestParam(value = "currencyId", required = false) String currencyId,
                               Model model) {
        if (currencyId != null) {
            userService.changeDefaultCurrency(currencyId);
            return createRedirectStatisticLink(startDate, endDate, userId, kindId, typeStr, priceStr, description, period);
        }

        if (period == null) {
            period = TypePeriod.SELECTED_PERIOD;
        }

        List<com.gorbatenko.budget.model.doc.User> users = new HashSet<>(budgetItemRepository.getUsersForAllPeriod())
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

        List<BudgetItem> listBudgetItems = budgetItemRepository.getFilteredData(offSetStartDate, offSetEndDate, userId, typeStr, kindId, priceStr, description, period);

        if ((typeStr != null) && (!("allTypes".equals(typeStr)))) {
            model.addAttribute("typeName", Type.valueOf(typeStr).getValue());
        }

        getBalanceParts(model, listBudgetItems, offSetStartDate.minusDays(1), offSetEndDate);
        TreeMap<LocalDate, List<BudgetItem>> map = listBudgetToTreeMap(listBudgetItems);
        model.addAttribute("startDate", dateToStr(startDate));
        model.addAttribute("endDate", dateToStr(endDate));
        model.addAttribute("listBudgetItems", map);
        model.addAttribute("users", users);
        model.addAttribute("userId", userId);
        model.addAttribute("kindList", getKinds());
        model.addAttribute("kindId", kindId);
        model.addAttribute("description", description);
        model.addAttribute("price", priceStr);

        model.addAttribute("pageName", "Статистика");
        model.addAttribute("detailToggler", true);
        model.addAttribute("filteredPage", true);

        return "budget/statistic";
    }

    private String createRedirectStatisticLink(LocalDate startDate, LocalDate endDate, String userId, String kindId, String typeStr, String priceStr, String description, TypePeriod period) {
        StringBuilder link = new StringBuilder("redirect:/budget/statistic?");
        if (startDate != null) {
            link.append("startDate="+dateToStr(startDate));
        }
        if (endDate != null) {
            link.append("&endDate="+dateToStr(endDate));
        }
        if (!userId.equals("-1")) {
            link.append("&userId="+userId);
        }
        if (!kindId.equals("-1")) {
            link.append("&kindId="+kindId);
        }
        if (!typeStr.equals("&allTypes")) {
            link.append("&type="+typeStr);
        }
        if (!priceStr.equals("")) {
            link.append("&price="+priceStr);
        }
        if (!description.equals("")) {
            link.append("&description="+description);
        }
        if (period != null) {
            link.append("&period="+period);
        }
        return link.toString();
    }

    @GetMapping("dynamicstatistic")
    public String getDynamicStatistic(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "kindId", defaultValue = "") String kindId,
            @RequestParam(value = "type", required = false, defaultValue = "") Type type,
            @RequestParam(value = "groupPeriod", required = false, defaultValue = "BY_DEFAULT") GroupPeriod groupPeriod,
            Model model) {

        if ((startDate == null) || (endDate == null)) {
            return "redirect:budget/groupstatistic";
        }

        if ((kindId.isEmpty() && type == null)) {
            return "redirect:budget/groupstatistic";
        }

        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;

        offSetStartDate = setTimeZoneOffset(startDate);
        offSetEndDate = setTimeZoneOffset(endDate).plusDays(1);

        String positionName;
        double positionSum;

        List<BudgetItem> listBudgetItems;

        if (groupPeriod == GroupPeriod.BY_DEFAULT) {
            groupPeriod = getGroupPeriod(startDate, endDate);
        }

        if (!kindId.isEmpty()) {
            Kind kind = kindRepository.getById(kindId);

            listBudgetItems =
                    budgetItemRepository.getFilteredData(offSetStartDate, offSetEndDate, null, null, kind.getId(), null, null, TypePeriod.SELECTED_PERIOD);

            positionName = kind.getName();
        } else {
            listBudgetItems =
                    budgetItemRepository.getFilteredData(offSetStartDate, offSetEndDate, null, type.name(), null, null, null, TypePeriod.SELECTED_PERIOD);

            positionName = type.getValue();
        }

        Map<String, Double> mapKind = listBudgetItems.stream()
                .collect(groupingBy(
                        (groupPeriod.equals(GroupPeriod.BY_DAYS) ? BudgetItem::getStrDate :
                                groupPeriod.equals(GroupPeriod.BY_MONTHS) ? BudgetItem::getStrYearMonth : BudgetItem::getStrYear),
                        Collectors.summingDouble(BudgetItem::getPrice)));

        TreeMap<String, Double> mapKindSort = new TreeMap<>(mapKind);

        positionSum = listBudgetItems.stream()
                .mapToDouble(BudgetItem::getPrice).sum();

        model.addAttribute("startDate", dateToStr(startDate));
        model.addAttribute("endDate", dateToStr(endDate));
        model.addAttribute("type", type);
        model.addAttribute("kindId", kindId);
        model.addAttribute("positionName", positionName);
        model.addAttribute("positionSum", positionSum);
        model.addAttribute("barChart", ChartUtil.createDynamicMdbChart(ChartType.BARCHART, positionName, mapKindSort));
        model.addAttribute("groupPeriod", groupPeriod);
        model.addAttribute("pageName", "Динамика");
        return "budget/dynamicstatistic";
    }

    @GetMapping("create/{type}")
    public String create(@PathVariable("type") String typeStr, Model model, TimeZone tz) {
        int sumTimezoneOffsetMinutes = getSumTimeZoneOffsetMinutes(tz);

        BudgetItem budgetItem = new BudgetItem();
        Type type = Type.valueOf(typeStr.toUpperCase());
        List<Kind> kinds = kindRepository.getFilteredData(null,null, type, false);

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

        budgetItem.setKind(kinds.get(0));

        String kindId = (model.asMap().containsKey("kindId") ? (String) model.asMap().get("kindId") : "");

        if(!kindId.isEmpty()) {
            budgetItem.setKind(kinds.stream()
                    .filter(k -> k.getId().equals(kindId)).findFirst().get());
        }

        budgetItem.setDate(LocalDateTime.of(
                LocalDateTime.now().plusMinutes(sumTimezoneOffsetMinutes).toLocalDate(),
                LocalTime.of(0,0)));
        budgetItem.setCurrency(getCurrencyDefault());

        List<Currency> currencies = currencyRepository.getFilteredData(null, null, false);

        model.addAttribute("budgetItem", budgetItem);
        model.addAttribute("type", type);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", getCurrencyDefault());

        model.addAttribute("pageName", "Создание");

        return "budget/edit";
    }

    @GetMapping("edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) throws Exception {
        BudgetItem budgetItem = budgetItemRepository.getById(id);
        if (budgetItem == null) {
            throw new Exception("Запись не найдена!");
        }
        Type type = budgetItem.getKind().getType();

        List<Kind> kinds = kindRepository.getFilteredData(null, null, type, false);
        kinds.sort(Comparator.comparing(Kind::getName));

        List<Currency> currencies = currencyRepository.getFilteredData(null, null, false);

        String kindId = (model.asMap().containsKey("kindId") ? (String) model.asMap().get("kindId") : "");

        if(!kindId.isEmpty()) {
            budgetItem.setKind(kinds.stream()
                    .filter(k -> k.getId().equals(kindId)).findFirst().get());
        }

        model.addAttribute("budgetItem", budgetItem);
        model.addAttribute("kinds", kinds);
        model.addAttribute("type", type);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", getCurrencyDefault());

        model.addAttribute("pageName", "Изменение");

        return "budget/edit";
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Response> delete(@PathVariable("id") String id) {
        budgetItemRepository.deleteById(id);
        return ResponseEntity.ok(new Response(200, null));
    }

    public static int getSumTimeZoneOffsetMinutes(TimeZone tz) {
        int userTimeZoneOffsetMinutes = tz.getRawOffset() / 1000 / 60;
        int currentTimeZoneOffsetMinutes = OffsetDateTime.now().getOffset().get(ChronoField.OFFSET_SECONDS) / 60;
        return (userTimeZoneOffsetMinutes + currentTimeZoneOffsetMinutes) * -1;
    }

    private com.gorbatenko.budget.model.doc.User toDocUser(com.gorbatenko.budget.model.User user) {
        return new com.gorbatenko.budget.model.doc.User(user.getId(), user.getName());
    }

    private GroupPeriod getGroupPeriod(LocalDate startDate, LocalDate endDate) {
        if ((endDate.getYear() == startDate.getYear()) &&
                (endDate.getMonth().equals(startDate.getMonth()))) {
            return GroupPeriod.BY_DAYS;
        } else {
            Period age = Period.between(startDate, endDate);
            if (age.getMonths() <= 12 && age.getYears() < 1) {
                return GroupPeriod.BY_MONTHS;
            }
            return GroupPeriod.BY_YEARS;
        }
    }

}

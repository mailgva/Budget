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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import static com.gorbatenko.budget.util.BaseUtil.*;
import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;


@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/budget/")
public class BudgetItemController extends AbstractWebController {

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createBudget(@Valid @RequestBody BudgetTo budgetTo, TimeZone tz) {
        String formatLink = "redirect:/budget/statistic?startDate=%s&endDate=%s#d_%s";
        if (budgetTo.getId().isEmpty()) {
            budgetTo.setId(null);
        }

        int sumTimezoneOffsetMinutes = getSumTimeZoneOffsetMinutes(tz);

        BudgetItem budgetItem = createBudgetFromBudgetTo(budgetTo);
        budgetItem.setCreateDateTime(LocalDateTime.now().plusMinutes(sumTimezoneOffsetMinutes));
        budgetItem.setId(budgetTo.getId());
        budgetItemService.save(budgetItem);

        LocalDate date = budgetItem.getDate().toLocalDate();

        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate endDate = LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth());

        // if day is in current month
        if (!(startDate.minusDays(1).isBefore(date) && (endDate.plusDays(1).isAfter(date)))) {
            startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
            endDate = LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth());
        }

        if (!budgetTo.getCurrencyId().equals(SecurityUtil.getCurrencyDefault().getId())) {
            userService.changeDefaultCurrency(budgetTo.getCurrencyId());
        }

        return String.format(formatLink, dateToStr(startDate), dateToStr(endDate), dateToStr(date));

    }

    private BudgetItem createBudgetFromBudgetTo(BudgetTo b) {
        Kind kind = kindService.getById(b.getKindId());
        Currency currency = currencyService.getById(b.getCurrencyId());
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
        GroupStatisticData result = budgetItemService.groupStatisticCollectData(period, startDate, endDate, sortType);

        model.addAttribute("profit", result.getProfit());
        model.addAttribute("spending", result.getSpending());
        model.addAttribute("remain", result.getProfit() - result.getSpending());
        model.addAttribute("remainOnStartPeriod", getRemainOnStartPeriod(result.getOffSetStartDate().minusDays(1)));
        model.addAttribute("remainOnEndPeriod", getRemainOnStartPeriod(result.getOffSetEndDate()));

        model.addAttribute("startDate", dateToStr(result.getStartDate()));
        model.addAttribute("endDate", dateToStr(result.getEndDate()));
        model.addAttribute("mapKindCount", result.getMapKindCount());
        model.addAttribute("mapKind", result.getMapKind());
        model.addAttribute("mapMaxPrice", result.getMapMaxPrice());

        model.addAttribute("circleChartProfit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.PROFIT, result.getMapKind()));
        model.addAttribute("circleChartSpendit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.SPENDING, result.getMapKind()));
        model.addAttribute("horizontChartProfit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.PROFIT, result.getMapKind()));
        model.addAttribute("horizontChartSpendit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.SPENDING, result.getMapKind()));

        model.addAttribute("totalBarChart", ChartUtil.createDynamicMultiMdbChart(ChartType.BARCHART, result.getTotalMap()));

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
                               Model model, TimeZone tz) {
        if (currencyId != null) {
            userService.changeDefaultCurrency(currencyId);
            return createRedirectStatisticLink(startDate, endDate, userId, kindId, typeStr, priceStr, description, period);
        }

        if (period == null) {
            period = TypePeriod.SELECTED_PERIOD;
        }

        StatisticData result = budgetItemService.statisticCollectData(startDate, endDate, userId, typeStr, kindId,
                priceStr, description, period);

        getBalanceParts(model, result.getListBudgetItems(),
                result.getOffSetStartDate().minusDays(1), result.getOffSetEndDate());

        if ((typeStr != null) && (!("allTypes".equals(typeStr)))) {
            model.addAttribute("typeName", Type.valueOf(typeStr).getValue());
        }

        model.addAttribute("startDate", dateToStr(result.getStartDate()));
        model.addAttribute("endDate", dateToStr(result.getEndDate()));
        model.addAttribute("listBudgetItems", listBudgetToTreeMap(result.getListBudgetItems(), tz));
        model.addAttribute("users", result.getUsers());
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

        DynamicStatisticData result = budgetItemService.dynamicStatisticCollectData(startDate, endDate,
                kindId, type, groupPeriod);

        model.addAttribute("startDate", dateToStr(result.getStartDate()));
        model.addAttribute("endDate", dateToStr(result.getEndDate()));
        model.addAttribute("type", type);
        model.addAttribute("kindId", kindId);
        model.addAttribute("positionName", result.getPositionName());
        model.addAttribute("positionSum", result.getPositionSum());
        model.addAttribute("barChart", ChartUtil.createDynamicMdbChart(ChartType.BARCHART, result.getPositionName(), result.getMapKindSort()));
        model.addAttribute("groupPeriod", groupPeriod);
        model.addAttribute("pageName", "Динамика");
        return "budget/dynamicstatistic";
    }

    @GetMapping("create/{type}")
    public String createBudgetItemByType(@PathVariable("type") String typeStr, Model model, TimeZone tz) {
        int sumTimezoneOffsetMinutes = getSumTimeZoneOffsetMinutes(tz);

        BudgetItem budgetItem = new BudgetItem();
        Type type = Type.valueOf(typeStr.toUpperCase());
        List<Kind> kinds = kindService.getKindsByType(type);

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

        List<Currency> currencies = currencyService.getByHidden(false);

        model.addAttribute("budgetItem", budgetItem);
        model.addAttribute("type", type);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", getCurrencyDefault());

        model.addAttribute("pageName", "Создание");

        return "budget/edit";
    }

    @GetMapping("edit/{id}")
    public String editBudgetItem(@PathVariable("id") String id, Model model) throws Exception {
        BudgetItem budgetItem = budgetItemService.getById(id);
        if (budgetItem == null) {
            throw new Exception("Запись не найдена!");
        }
        Type type = budgetItem.getKind().getType();

        List<Kind> kinds = kindService.getKindsByType(type);
        kinds.sort(Comparator.comparing(Kind::getName));

        List<Currency> currencies = currencyService.getByHidden(false);

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
    public ResponseEntity<Response> deleteBudgetItem(@PathVariable("id") String id) {
        budgetItemService.deleteById(id);
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

}

package com.gorbatenko.budget.web;


import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.service.*;
import com.gorbatenko.budget.to.BudgetItemTo;
import com.gorbatenko.budget.to.ExchangeTo;
import com.gorbatenko.budget.util.*;
import com.gorbatenko.budget.web.charts.ChartType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.gorbatenko.budget.model.Kind.EXCHANGE_NAME;
import static com.gorbatenko.budget.util.BaseUtil.dateToStr;
import static com.gorbatenko.budget.util.BaseUtil.listBudgetToTreeMap;
import static com.gorbatenko.budget.util.SecurityUtil.get;
import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;
import static com.gorbatenko.budget.util.Utils.DEFAULT_UUID;
import static com.gorbatenko.budget.util.Utils.equalsUUID;


@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/budget/")
public class BudgetItemController extends BaseWebController {
    private final UserService userService;

    public BudgetItemController(CurrencyService currencyService, KindService kindService,
                                BudgetItemService budgetItemService, UserService userService) {
        super(currencyService, kindService, budgetItemService);
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createBudget(@Valid @RequestBody BudgetItemTo budgetItemTo) {
        String formatLink = "redirect:/budget/statistic?startDate=%s&endDate=%s#d_%s";
        BudgetItem budgetItem = createBudgetFromBudgetTo(budgetItemTo);
        budgetItem.setCreatedAt(LocalDateTime.now());
        budgetItem.setId(budgetItemTo.getId());
        budgetItemService.save(budgetItem);

        LocalDate date = budgetItem.getDateAt();

        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate endDate = LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth());

        // if day is in current month
        if (!(startDate.minusDays(1).isBefore(date) && (endDate.plusDays(1).isAfter(date)))) {
            startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
            endDate = LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth());
        }

        if (!equalsUUID(budgetItemTo.getCurrencyId(), SecurityUtil.getCurrencyDefault().getId())) {
            userService.changeDefaultCurrency(budgetItemTo.getCurrencyId());
        }

        return String.format(formatLink, dateToStr(startDate), dateToStr(endDate), dateToStr(date));
    }

    private BudgetItem createBudgetFromBudgetTo(BudgetItemTo b) {
        Kind kind = kindService.findById(b.getKindId());
        Currency currency = currencyService.findById(b.getCurrencyId());
        return new BudgetItem(get().getUser(),
                kind,
                b.getDateAt(),
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

        model.addAttribute("remainOnStartPeriod", result.getRemainOnStartPeriod());
        model.addAttribute("remainOnEndPeriod", result.getRemainOnEndPeriod());

        model.addAttribute("startDate", dateToStr(result.getStartDate()));
        model.addAttribute("endDate", dateToStr(result.getEndDate()));
        model.addAttribute("mapKindCount", result.getMapKindCount());
        model.addAttribute("mapKind", result.getMapKind());
        model.addAttribute("mapMaxPrice", result.getMapMaxPrice());

        model.addAttribute("circleChartProfit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.PROFIT, result.getMapKind()));
        model.addAttribute("circleChartSpendit", ChartUtil.createMdbChart(ChartType.DOUGHNUT, Type.SPENDING, result.getMapKind()));
        model.addAttribute("horizontChartProfit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.PROFIT, result.getMapKind()));
        model.addAttribute("horizontChartSpendit", ChartUtil.createMdbChart(ChartType.HORIZONTALBAR, Type.SPENDING, result.getMapKind()));

        model.addAttribute("totalBarChart", ChartUtil.createDynamicMultiMdbChart(ChartType.BAR, result.getTotalMap()));
        model.addAttribute("dynamicBarChart", ChartUtil.createMdbChart(ChartType.LINE, "Динамика остатков", result.getDynamicRemain()));

        model.addAttribute("period", period);
        model.addAttribute("sortType", sortType);
        model.addAttribute("groupPeriod", result.getGroupPeriod());


        model.addAttribute("pageName", "Групповая статистика");
        model.addAttribute("filteredPage", true);

        return "budget/groupstatistic";
    }

    @GetMapping("statistic")
    public String getStatistic(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                               @RequestParam(value = "userId", required = false) UUID userId,
                               @RequestParam(value = "kindId", required = false) UUID kindId,
                               @RequestParam(value = "type", defaultValue = "allTypes") String typeStr,
                               @RequestParam(value = "price", required = false) String priceStr,
                               @RequestParam(value = "description", required = false) String description,
                               @RequestParam(value = "period", required = false) TypePeriod period,
                               @RequestParam(value = "currencyId", required = false) UUID currencyId,
                               Model model) {
        if (currencyId != null && !equalsUUID(get().getUser().getCurrencyDefault().getId(), currencyId)) {
            userService.changeDefaultCurrency(currencyId);
            return createRedirectStatisticLink(startDate, endDate, userId, kindId, typeStr, priceStr, description, period);
        }

        if (period == null) {
            period = TypePeriod.SELECTED_PERIOD;
        }

        LocalDate now = LocalDate.now();

        if (startDate == null) {
            startDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
        }

        if (endDate == null) {
            endDate = LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth());
        }

        if (userId != null && DEFAULT_UUID.compareTo(userId) == 0) {
            userId = null;
        }

        if (kindId != null && DEFAULT_UUID.compareTo(kindId) == 0) {
            kindId = null;
        }

        Type type = "allTypes".equalsIgnoreCase(typeStr) ? null : Type.valueOf(typeStr);

        description = !StringUtils.hasText(description) ? null : description;
        priceStr = !StringUtils.hasText(priceStr) ? null : priceStr;

        StatisticData result = budgetItemService.statisticCollectData(startDate, endDate, userId, type, kindId,
                priceStr, description, period);

        getBalanceParts(model, result.getListBudgetItems(), startDate, endDate);

        if (type != null) {
            model.addAttribute("typeName", type.getValue());
        }
        model.addAttribute("defaultUuid", DEFAULT_UUID);

        model.addAttribute("startDate", dateToStr(result.getStartDate()));
        model.addAttribute("endDate", dateToStr(result.getEndDate()));
        model.addAttribute("listBudgetItems", listBudgetToTreeMap(result.getListBudgetItems()));
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

    private String createRedirectStatisticLink(LocalDate startDate, LocalDate endDate, UUID userId, UUID kindId, String typeStr, String priceStr, String description, TypePeriod period) {
        StringBuilder link = new StringBuilder("redirect:/budget/statistic?");
        if (startDate != null) {
            link.append("startDate="+dateToStr(startDate));
        }
        if (endDate != null) {
            link.append("&endDate="+dateToStr(endDate));
        }
        if (userId != null && !equalsUUID(DEFAULT_UUID, userId)) {
            link.append("&userId="+userId);
        }
        if (kindId != null && !equalsUUID(DEFAULT_UUID, kindId)) {
            link.append("&kindId="+kindId);
        }
        if (!typeStr.equals("&allTypes")) {
            link.append("&type="+typeStr);
        }
        if (StringUtils.hasText(priceStr)) {
            link.append("&price="+priceStr);
        }
        if (StringUtils.hasText(description)) {
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
            @RequestParam(value = "kindId", defaultValue = "") UUID kindId,
            @RequestParam(value = "type", required = false, defaultValue = "") Type type,
            @RequestParam(value = "groupPeriod", required = false, defaultValue = "BY_DEFAULT") GroupPeriod groupPeriod,
            Model model) {

        if ((startDate == null) || (endDate == null)) {
            return "redirect:budget/groupstatistic";
        }

        if ((kindId == null && type == null)) {
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
        model.addAttribute("barChart", ChartUtil.createDynamicMdbChart(ChartType.BAR, result.getPositionName(), result.getMapKindSort()));
        model.addAttribute("groupPeriod", result.getGroupPeriod());
        model.addAttribute("pageName", "Динамика");
        return "budget/dynamicstatistic";
    }

    @GetMapping("create/{type}")
    public String createBudgetItemByType(@PathVariable("type") String typeStr, Model model) {
        BudgetItem budgetItem = new BudgetItem();
        Type type = Type.valueOf(typeStr.toUpperCase());
        List<Kind> kinds = kindService.findByType(type);

        if (kinds.size() == 0) {
            return "redirect:/dictionaries/kinds/create/"+typeStr;
        }

        kinds.sort(Comparator.comparing(Kind::getName));

        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate endDate = LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth());

        kinds = sortKindsByPopular(kinds, type, startDate, endDate);
        budgetItem.setKind(kinds.get(0));

        UUID kindId = (model.asMap().containsKey("kindId") ? (UUID) model.asMap().get("kindId") : null);

        if(kindId != null) {
            budgetItem.setKind(kinds.stream()
                    .filter(kind -> equalsUUID(kind.getId(), kindId))
                    .findFirst().get()
            );
        }

        budgetItem.setDateAt(now);
        budgetItem.setCurrency(getCurrencyDefault());

        List<Currency> currencies = currencyService.findAllVisible();

        model.addAttribute("budgetItem", budgetItem);
        model.addAttribute("type", type);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", getCurrencyDefault());

        model.addAttribute("pageName", "Создание");

        return "budget/edit";
    }

    @GetMapping("edit/{id}")
    public String editBudgetItem(@PathVariable("id") UUID id, Model model) throws Exception {
        BudgetItem budgetItem = budgetItemService.getById(id);
        if (budgetItem == null) {
            throw new Exception("Запись не найдена!");
        }
        Type type = budgetItem.getKind().getType();

        List<Kind> kinds = kindService.findByType(type);
        kinds.sort(Comparator.comparing(Kind::getName));

        List<Currency> currencies = currencyService.findAllVisible();

        UUID kindId = (model.asMap().containsKey("kindId") ? (UUID) model.asMap().get("kindId") : null);

        if(kindId != null) {
            budgetItem.setKind(kinds.stream()
                    .filter(k -> equalsUUID(k.getId(), kindId))
                    .findFirst().get()
            );
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
    public ResponseEntity<Response> deleteBudgetItem(@PathVariable("id") UUID id) {
        budgetItemService.deleteById(id);
        return ResponseEntity.ok(new Response(200, null));
    }

    @GetMapping("exchange")
    public String exchange(Model model) {
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        model.addAttribute("currentDate", currentDate.format(formatter));
        model.addAttribute("currencies", currencyService.findAllVisible());
        model.addAttribute("defaultCurrency", getCurrencyDefault());
        model.addAttribute("pageName", "Обмен валюты");
        return "budget/exchange";
    }

    @PostMapping("exchange")
    public String doExchange(@Valid @RequestBody ExchangeTo exchange, HttpServletRequest request) {
        String formatLink = "redirect:/budget/statistic?startDate=%s&endDate=%s#d_%s";

        LocalDate commonDate = exchange.getDate();
        LocalDateTime commonDateTime = LocalDateTime.now();
        User commonUser = get().getUser();

        BudgetItem from = new BudgetItem();
        from.setDateAt(commonDate);
        from.setUser(commonUser);
        from.setKind(kindService.findByNameAndType(Type.SPENDING, EXCHANGE_NAME));
        from.setCurrency(currencyService.findById(exchange.getFromCurrencyId()));
        from.setDescription(exchange.getDescription());
        from.setPrice(exchange.getFromAmount());
        from.setCreatedAt(commonDateTime);

        BudgetItem to = new BudgetItem();
        to.setDateAt(commonDate);
        to.setUser(commonUser);
        to.setKind(kindService.findByNameAndType(Type.SPENDING, EXCHANGE_NAME));
        to.setCurrency(currencyService.findById(exchange.getToCurrencyId()));
        to.setDescription(exchange.getDescription());
        to.setPrice(exchange.getToAmount());
        to.setCreatedAt(commonDateTime);

        List<BudgetItem> list = new ArrayList<>();
        list.add(from);
        list.add(to);

        budgetItemService.saveAll(list);

        LocalDate date = exchange.getDate();

        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate endDate = LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth());

        // if day is in current month
        if (!(startDate.minusDays(1).isBefore(date) && (endDate.plusDays(1).isAfter(date)))) {
            startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
            endDate = LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth());
        }

        if (!equalsUUID(exchange.getToCurrencyId(), getCurrencyDefault().getId())) {
            userService.changeDefaultCurrency(exchange.getToCurrencyId());
        }

        return String.format(formatLink, dateToStr(startDate), dateToStr(endDate), dateToStr(date));
    }

    @GetMapping("dynamicRemainStatistic")
    @ResponseBody
    public String dynamicRemainStatistic(
            @RequestParam(value = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "groupPeriod", defaultValue = "BY_DEFAULT") GroupPeriod groupPeriod) {
        TreeMap<String, Double> data = budgetItemService.createDynamicRemainStatistic(startDate, endDate, groupPeriod);
        String json = ChartUtil.createMdbChart(ChartType.LINE, "Динамика остатков", data);
        return json;
    }

    private List<Kind> sortKindsByPopular(List<Kind> listKind, Type type, LocalDate startDate, LocalDate endDate) {
        final int POPULAR_KIND_COUNT = 5;

        List<Kind> result = budgetItemService.getPopularKindByTypeForPeriod(type, startDate, endDate, POPULAR_KIND_COUNT) ;

        for(Kind kind : listKind) {
            if (!result.contains(kind)) {
                result.add(kind);
            }
        }

        return result;
    }

}

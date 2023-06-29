package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.repository.BudgetItemRepository;
import com.gorbatenko.budget.repository.CurrencyRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.util.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.BaseUtil.setTimeZoneOffset;
import static java.util.stream.Collectors.groupingBy;

@Service
public class BudgetItemService {
    private BudgetItemRepository budgetItemRepository;
    private KindRepository kindRepository;
    private CurrencyRepository currencyRepository;

    public BudgetItemService(BudgetItemRepository budgetItemRepository, KindRepository kindRepository, CurrencyRepository currencyRepository) {
        this.budgetItemRepository = budgetItemRepository;
        this.kindRepository = kindRepository;
        this.currencyRepository = currencyRepository;
    }

    public BudgetItem save(BudgetItem budgetItem) {
        return budgetItemRepository.save(budgetItem);
    }

    public void saveAll(List<BudgetItem> budgetItems) {
        budgetItemRepository.saveAll(budgetItems);
    }

    public List<KindTotals> getTotalsByKindsForPeriod(LocalDateTime startDate, LocalDateTime endDate, TypePeriod period) {
        return budgetItemRepository.getTotalsByKindsForPeriod(startDate, endDate, period);
    }

    public List<BudgetItem> getByCurrencyId(String id) {
        return budgetItemRepository.getByCurrencyId(id);
    }

    public List<BudgetItem> getByKindId(String id) {
        return budgetItemRepository.getByKindId(id);
    }

    public List<BudgetItem> getPopularByTypeForPeriod(LocalDateTime startDate, LocalDateTime endDate, String typeName) {
        return budgetItemRepository.getFilteredData(startDate, endDate, null, typeName, null, null, null, TypePeriod.SELECTED_PERIOD);
    }

    public List<BudgetItem> getBeforeDate(LocalDateTime endDate) {
        return budgetItemRepository.getFilteredData(null, endDate, null, null, null, null, null, TypePeriod.SELECTED_PERIOD);
    }

    public List<BudgetItem> getAll() {
        return budgetItemRepository.getAll();
    }

    public BudgetItem getById(String id) {
        return budgetItemRepository.getById(id);
    }

    public List<CurrencyCount> getCurrencyCounts() {
        return budgetItemRepository.getCurrencyCounts();
    }

    public List<BudgetItem> getByUserId(String id) {
        return budgetItemRepository.getFilteredData(null,null, id, null, null, null, null, TypePeriod.ALL_TIME);
    }

    public List<BudgetItem> getForSelectedPeriod(LocalDateTime startLocalDate, LocalDateTime endLocalDate) {
        return budgetItemRepository.getFilteredData(startLocalDate, endLocalDate, null, null, null, null, null, TypePeriod.SELECTED_PERIOD);
    }

    public Double getSumPriceByCurrencyAndType(Currency currency, Type type) {
        return budgetItemRepository.getSumPriceByCurrencyAndType(currency, type);
    }

    public Double getSumPriceByDefaultCurrencyAndType(Type type) {
        return budgetItemRepository.getSumPriceByDefaultCurrencyAndType(type);
    }

    public LocalDateTime getMaxDate() {
        return budgetItemRepository.getMaxDate();
    }

    public String getLastCurrencyIdByDate(LocalDate date) {
        return budgetItemRepository.getLastCurrencyIdByDate(date);
    }
    public void deleteById(String id) {
        budgetItemRepository.deleteById(id);
    }

    public GroupStatisticData groupStatisticCollectData(TypePeriod period, LocalDate startDate, LocalDate endDate, String sortType) {
        GroupStatisticData result = new GroupStatisticData();

        LocalDate now = LocalDate.now();
        LocalDateTime offSetStartDate;
        LocalDateTime offSetEndDate;
        TreeMap<Type, Map<Kind, Double>> mapKind;

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

        List<KindTotals> totals = getTotalsByKindsForPeriod(offSetStartDate, offSetEndDate, period);

        if (period.equals(TypePeriod.ALL_TIME) && totals.size() > 0) {
            offSetStartDate = totals.stream()
                    .map(total -> total.getMinCreateDateTime())
                    .min(LocalDateTime::compareTo)
                    .get();
        }

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

        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setOffSetStartDate(offSetStartDate);
        result.setOffSetEndDate(offSetEndDate);
        result.setMapKind(mapKind);
        result.setMapKindCount(mapKindCount);
        result.setMapMaxPrice(mapMaxPrice);
        result.setTotalMap(totalMap);
        result.setProfit(profit);
        result.setSpending(spending);

        return result;
    }

    public StatisticData statisticCollectData(LocalDate startDate, LocalDate endDate, String userId, String typeStr,
                                              String kindId, String priceStr, String description, TypePeriod period) {
        StatisticData result = new StatisticData();

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

        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setOffSetStartDate(offSetStartDate);
        result.setOffSetEndDate(offSetEndDate);
        result.setListBudgetItems(listBudgetItems);
        result.setUsers(users);
        return result;
    }

    public DynamicStatisticData dynamicStatisticCollectData(LocalDate startDate, LocalDate endDate,
                                                            String kindId, Type type, GroupPeriod groupPeriod) {
        DynamicStatisticData result = new DynamicStatisticData();

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


        positionSum = listBudgetItems.stream()
                .mapToDouble(BudgetItem::getPrice).sum();

        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setOffSetStartDate(offSetStartDate);
        result.setOffSetEndDate(offSetEndDate);
        result.setMapKindSort(new TreeMap<>(mapKind));
        result.setPositionName(positionName);
        result.setPositionSum(positionSum);
        return result;
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

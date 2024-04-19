package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.repository.BudgetItemRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.model.Type.PROFIT;
import static com.gorbatenko.budget.model.Type.SPENDING;
import static com.gorbatenko.budget.util.TypePeriod.*;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class BudgetItemService {
    private final BudgetItemRepository budgetItemRepository;
    private final KindRepository kindRepository;

    public BudgetItem save(BudgetItem budgetItem) {
        return budgetItemRepository.save(budgetItem);
    }

    public void saveAll(List<BudgetItem> budgetItems) {
        budgetItemRepository.saveAll(budgetItems);
    }

    public List<KindTotals> getTotalsByKindsForPeriod(LocalDate startDate, LocalDate endDate, TypePeriod period) {
        return budgetItemRepository.getTotalsByKindsForPeriod(startDate, endDate, period);
    }

    public List<BudgetItem> findByCurrencyId(UUID id) {
        return budgetItemRepository.findByCurrencyId(id);
    }

    public List<BudgetItem> findByKindId(UUID id) {
        return budgetItemRepository.findByKindId(id);
    }
    public List<BudgetItem> findAll() {
        return budgetItemRepository.findAll();
    }

    public BudgetItem getById(UUID id) {
        return budgetItemRepository.findById(id);
    }

    public TreeMap<Currency, Long> getCurrencyCounts() {
        return budgetItemRepository.getCurrencyCounts();
    }

    public TreeMap<Kind, Long> getKindCounts() {return budgetItemRepository.getKindCounts(); }

    public List<BudgetItem> findBySelectedPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        return budgetItemRepository.findBySelectedPeriod(startLocalDate, endLocalDate);
    }

    public Double getSumPriceByCurrencyAndType(Currency currency, Type type) {
        return budgetItemRepository.getSumPriceByCurrencyAndType(currency, type);
    }

    public Double getSumPriceByDefaultCurrencyAndType(Type type) {
        return budgetItemRepository.getSumPriceByDefaultCurrencyAndType(type);
    }

    public Double getRemainByDefaultCurrencyForDate(LocalDate date) {
        return budgetItemRepository.getRemainByDefaultCurrencyForDate(date);
    }

    public LocalDate findMaxDate() {
        return budgetItemRepository.getMaxDate();
    }

    public UUID findLastCurrencyIdByDate(LocalDate date) {
        return budgetItemRepository.findLastCurrencyId(date);
    }
    public void deleteById(UUID id) {
        budgetItemRepository.deleteById(id);
    }

    public GroupStatisticData groupStatisticCollectData(TypePeriod period, LocalDate startDate, LocalDate endDate, String sortType) {
        GroupStatisticData result = new GroupStatisticData();

        LocalDate now = LocalDate.now();
        TreeMap<Type, Map<Kind, Double>> mapKind;

        if ((startDate == null) || (period.equals(CURRENT_MONTH))) {
            startDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
            endDate = LocalDate.of(now.getYear(), now.getMonth(), now.lengthOfMonth());
        }

        if (period.equals(CURRENT_YEAR)) {
            startDate = LocalDate.of(now.getYear(), 1, 1);
            endDate = LocalDate.of(now.getYear(), 12, 31);
        }

        List<KindTotals> totals = getTotalsByKindsForPeriod(startDate, endDate, period);

        if (period.equals(ALL_TIME) && totals.size() > 0) {
            startDate = totals.stream()
                    .map(total -> total.getMinCreateDate())
                    .min(LocalDate::compareTo)
                    .get();
        }

        if (sortType.isEmpty() || sortType.equalsIgnoreCase("byName")) {
            mapKind = new TreeMap(totals.stream()
                    .collect(groupingBy(total ->
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
                    .map(KindTotals::getMinCreateDate)
                    .min(LocalDate::compareTo)
                    .get();

            endDate = totals.stream()
                    .map(KindTotals::getMaxCreateDate)
                    .max(LocalDate::compareTo)
                    .get();
        }

        GroupPeriod groupPeriod = getGroupPeriod(startDate, endDate);

        Map<String, Double> mapDateProfit = budgetItemRepository.getSumPriceForPeriodByDateAndDefaultCurrency(
                startDate, endDate, PROFIT, period, groupPeriod);

        Map<String, Double> mapDateSpending = budgetItemRepository.getSumPriceForPeriodByDateAndDefaultCurrency(
                startDate, endDate, SPENDING, period, groupPeriod);

        TreeMap<String, TreeMap<Type, Double>> totalMap = new TreeMap<>();
        fillTotalMap(totalMap, mapDateProfit, PROFIT);
        fillTotalMap(totalMap, mapDateSpending, SPENDING);


        Map<Kind, Long> mapKindCount = totals.stream()
                .collect(Collectors.toMap(KindTotals::getKind, KindTotals::getCount));

        Double maxPriceProfit = totals.stream()
                .filter(total -> total.getKind().getType().equals(PROFIT))
                .map(total -> total.getSumPrice())
                .max(Double::compareTo)
                .orElse(0.0);

        Double maxPriceSpending = totals.stream()
                .filter(total -> total.getKind().getType().equals(SPENDING))
                .map(total -> total.getSumPrice())
                .max(Double::compareTo)
                .orElse(0.0);

        Map<Type, Double> mapMaxPrice = new HashMap<>();
        mapMaxPrice.put(PROFIT, maxPriceProfit);
        mapMaxPrice.put(SPENDING, maxPriceSpending);

        Double profit = totals.stream()
                .filter(total -> total.getKind().getType().equals(PROFIT))
                .mapToDouble(KindTotals::getSumPrice)
                .sum();

        Double spending = totals.stream()
                .filter(total -> total.getKind().getType().equals(SPENDING))
                .mapToDouble(KindTotals::getSumPrice)
                .sum();

        Double remainOnStartPeriod = period.equals(ALL_TIME) ? 0.0D : getRemainByDefaultCurrencyForDate(startDate);
        Double remainOnEndPeriod = getRemainByDefaultCurrencyForDate(endDate);
        Double remain = remainOnStartPeriod;
        TreeSet<String> keys = new TreeSet<>() {{
            addAll(mapDateProfit.keySet());
            addAll(mapDateSpending.keySet());}};

        TreeMap<String, Double> dynamicRemain = new TreeMap<>();
        for (String key : keys) {
            Double value = mapDateProfit.getOrDefault(key, 0.0D) -
                    mapDateSpending.getOrDefault(key, 0.0D);
            remain += value;
            dynamicRemain.put(key, remain);
        }

        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setMapKind(mapKind);
        result.setMapKindCount(mapKindCount);
        result.setMapMaxPrice(mapMaxPrice);
        result.setTotalMap(totalMap);
        result.setProfit(profit);
        result.setSpending(spending);
        result.setDynamicRemain(dynamicRemain);
        result.setRemainOnStartPeriod(remainOnStartPeriod);
        result.setRemainOnEndPeriod(remainOnEndPeriod);
        result.setGroupPeriod(groupPeriod);

        return result;
    }

    private void fillTotalMap(TreeMap<String, TreeMap<Type, Double>> totalMap, Map<String, Double> inputMap, Type type) {
        for(Map.Entry<String, Double> entry : inputMap.entrySet()) {
            TreeMap<Type, Double> map = totalMap.getOrDefault(entry.getKey(), new TreeMap<>());
            double value = map.getOrDefault(type, 0.00D) + entry.getValue();
            map.put(type, value);
            totalMap.put(entry.getKey(), map);
        }
    }

    public StatisticData statisticCollectData(LocalDate startDate, LocalDate endDate, UUID userId, Type type,
                                              UUID kindId, String priceStr, String description, TypePeriod period) {
        StatisticData result = new StatisticData();

        List<User> users = budgetItemRepository.getUsersForAllPeriod();

        List<BudgetItem> listBudgetItems = budgetItemRepository.getFilteredData(startDate, endDate, userId, type, kindId, priceStr, description, period);

        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setListBudgetItems(listBudgetItems);
        result.setUsers(users);
        return result;
    }

    public DynamicStatisticData dynamicStatisticCollectData(LocalDate startDate, LocalDate endDate,
                                                            UUID kindId, Type type, GroupPeriod groupPeriod) {
        DynamicStatisticData result = new DynamicStatisticData();

        String positionName;
        double positionSum;

        List<BudgetItem> listBudgetItems;

        if (groupPeriod == GroupPeriod.BY_DEFAULT) {
            groupPeriod = getGroupPeriod(startDate, endDate);
        }

        if (kindId != null) {
            Kind kind = kindRepository.findById(kindId);
            listBudgetItems = budgetItemRepository.findByKindIdAndSelectedPeriod(kind.getId(), startDate, endDate);
            positionName = kind.getName();
        } else {
            listBudgetItems = budgetItemRepository.findByTypeAndSelectedPeriod(type, startDate, endDate);
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
        result.setMapKindSort(new TreeMap<>(mapKind));
        result.setPositionName(positionName);
        result.setPositionSum(positionSum);
        result.setGroupPeriod(groupPeriod);
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

    public TreeMap<String, Double> createDynamicRemainStatistic(LocalDate startDate, LocalDate endDate, GroupPeriod groupPeriod) {
        Map<String, Double> mapDateProfit =
                budgetItemRepository.getSumPriceForPeriodByDateAndDefaultCurrency(
                        startDate, endDate, PROFIT, SELECTED_PERIOD, groupPeriod);

        Map<String, Double> mapDateSpending =
                budgetItemRepository.getSumPriceForPeriodByDateAndDefaultCurrency(
                        startDate, endDate, SPENDING, SELECTED_PERIOD, groupPeriod);

        Double remain = getRemainByDefaultCurrencyForDate(startDate);
        TreeSet<String> keys = new TreeSet<>() {{
            addAll(mapDateProfit.keySet());
            addAll(mapDateSpending.keySet());}};

        TreeMap<String, Double> dynamicRemain = new TreeMap<>();
        for (String key : keys) {
            Double value = mapDateProfit.getOrDefault(key, 0.0D) -
                    mapDateSpending.getOrDefault(key, 0.0D);
            remain += value;
            dynamicRemain.put(key, remain);
        }

        return dynamicRemain;
    }

    public List<Kind> getPopularKindByTypeForPeriod(Type type, LocalDate startDate, LocalDate endDate, int popularCount) {
        return budgetItemRepository.getPopularKindByTypeForPeriod(type, startDate, endDate, popularCount);
    }

}

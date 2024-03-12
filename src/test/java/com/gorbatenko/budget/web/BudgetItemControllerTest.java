package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.util.DynamicStatisticData;
import com.gorbatenko.budget.util.GroupStatisticData;
import com.gorbatenko.budget.util.SecurityUtil;
import com.gorbatenko.budget.util.StatisticData;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BudgetItemControllerTest extends AbstractWebControllerTest{

    private static final String CONTROLLER_PATH = "/budget/";

    @Test
    void createBudgetItemPost() throws Exception {
        String path = CONTROLLER_PATH;
        when(budgetItemService.save(any())).thenReturn(BUDGET_ITEM);
        when(kindService.findById(any())).thenReturn(BUDGET_ITEM.getKind());
        when(currencyService.findById(any())).thenReturn(BUDGET_ITEM.getCurrency());

        try (MockedStatic<SecurityUtil> utils = Mockito.mockStatic(SecurityUtil.class)) {
            utils.when(() -> SecurityUtil.getCurrencyDefault()).thenReturn(BUDGET_ITEM.getCurrency());
            utils.when(() -> SecurityUtil.get()).thenReturn(AUTHORIZED_USER);

            mockMvc.perform(post(path).with(CSRF).params(PARAMS_CSRF_TOKEN)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("id", "")
                            .param("type", BUDGET_ITEM.getKind().getType().getValue())
                            .param("kindId", BUDGET_ITEM.getKind().getId().toString())
                            .param("currencyId", BUDGET_ITEM.getCurrency().getId().toString())
                            .param("date", BUDGET_ITEM.getDateAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            .param("description", BUDGET_ITEM.getDescription())
                            .param("price", BUDGET_ITEM.getPrice().toString())
                    )
                    //.andDo(print())
                    .andExpect(redirectedUrl("/budget/statistic?startDate=2023-06-01&endDate=2023-06-30#d_2023-06-04"));
        }
    }

    @Test
    void getGroupStatistic() throws Exception {
        String path = CONTROLLER_PATH+"groupstatistic";

        GroupStatisticData result = new GroupStatisticData();
        result.setStartDate(LocalDate.now());
        result.setEndDate(LocalDate.now());
        result.setProfit(1000.00);
        result.setSpending(0.0);
        result.setMapKindCount(Map.of(BUDGET_ITEM.getKind(), 1L));
        result.setMapMaxPrice(Map.of(BUDGET_ITEM.getKind().getType(), result.getProfit()));
        TreeMap<String, TreeMap<Type, Double>> map = new TreeMap<>();
        TreeMap<Type, Double> mapIn = new TreeMap<>();
        mapIn.put(BUDGET_ITEM.getKind().getType(), result.getProfit());
        map.put(result.getStartDate().toString(), mapIn);
        result.setTotalMap(map);
        TreeMap<Type, Map<Kind, Double>> mapKind = new TreeMap<>();
        mapKind.put(BUDGET_ITEM.getKind().getType(), Map.of(BUDGET_ITEM.getKind(), result.getProfit()));
        result.setMapKind(mapKind);
        result.setUsers(List.of(TEST_USER));

        when(budgetItemService.groupStatisticCollectData(any(), any(), any(), any())).thenReturn(result);

        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("pageName", "Групповая статистика"))
                .andExpect(view().name("budget/groupstatistic"));
    }

    @Test
    void getStatistic() throws Exception {
        String path = CONTROLLER_PATH+"statistic";

        StatisticData result = new StatisticData();
        result.setStartDate(LocalDate.now());
        result.setEndDate(LocalDate.now());
        result.setListBudgetItems(List.of(BUDGET_ITEM));
        result.setUsers(List.of(TEST_USER));

        when(budgetItemService.statisticCollectData(any(), any(), any(), any(),any(), any(), any(), any())).thenReturn(result);

        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("pageName", "Статистика"))
                .andExpect(view().name("budget/statistic"));
    }

    @Test
    void getDynamicStatistic() throws Exception {
        String date = LocalDate.now().format(DATE_FORMATTER);
        String path = CONTROLLER_PATH+"dynamicstatistic?startDate="+date+"&endDate="+date+"&kindId="+BUDGET_ITEM.getKind().getId();

        DynamicStatisticData result = new DynamicStatisticData();
        result.setStartDate(LocalDate.now());
        result.setEndDate(LocalDate.now());
        result.setPositionSum(BUDGET_ITEM.getPrice());
        result.setPositionName(BUDGET_ITEM.getKind().getName());
        TreeMap<String, Double> mapKindSort = new TreeMap<>();
        mapKindSort.put(BUDGET_ITEM.getKind().getName(), BUDGET_ITEM.getPrice());
        result.setMapKindSort(mapKindSort);

        when(budgetItemService.dynamicStatisticCollectData(any(), any(), any(), any(),any())).thenReturn(result);

        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                .andExpect(model().attribute("pageName", "Динамика"))
                .andExpect(view().name("budget/dynamicstatistic"));
    }

    @Test
    void createBudgetItemByType() throws Exception {
        String path = CONTROLLER_PATH+"create/PROFIT";
        List<Kind> kinds = Arrays.asList(BUDGET_ITEM.getKind());

        when(kindService.findByType(any())).thenReturn(kinds);
        when(budgetItemService.getPopularKindByTypeForPeriod(any(), any(), any(), any())).thenReturn(new ArrayList<>());
        when(currencyService.findAllVisible()).thenReturn(List.of(BUDGET_ITEM.getCurrency()));

        try (MockedStatic<SecurityUtil> utils = Mockito.mockStatic(SecurityUtil.class)) {
            utils.when(() -> SecurityUtil.getCurrencyDefault()).thenReturn(BUDGET_ITEM.getCurrency());

            mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                    //.andDo(print())
                    .andExpect(model().attribute("pageName", "Создание"))
                    .andExpect(view().name("budget/edit"));
        }
    }

    @Test
    void editBudgetItem() throws Exception {
        String path = CONTROLLER_PATH+"edit/"+BUDGET_ITEM.getId();

        when(budgetItemService.getById(BUDGET_ITEM.getId())).thenReturn(BUDGET_ITEM);
        when(kindService.findByType(BUDGET_ITEM.getKind().getType())).thenReturn(Arrays.asList(BUDGET_ITEM.getKind()));
        when(currencyService.findAllVisible()).thenReturn(List.of(BUDGET_ITEM.getCurrency()));

        try (MockedStatic<SecurityUtil> utils = Mockito.mockStatic(SecurityUtil.class)) {
            utils.when(() -> SecurityUtil.getCurrencyDefault()).thenReturn(BUDGET_ITEM.getCurrency());

            mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                    //.andDo(print())
                    .andExpect(model().attribute("pageName", "Изменение"))
                    .andExpect(view().name("budget/edit"));
        }
    }

    @Test
    void deleteBudgetItem() throws Exception {
        String path = CONTROLLER_PATH+BUDGET_ITEM.getId();
        mockMvc.perform(delete(path)).andExpect(status().isOk());
    }

}
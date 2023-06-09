package com.gorbatenko.budget.web;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class MainControllerTest extends AbstractWebControllerTest {
    @Test
    void getMain() throws Exception {
        String path = "/";
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                .andExpect(view().name("login"))
                .andExpect(status().isOk());
    }

    @Test
    void getMenu() throws Exception {
        String path = "/menu";

        when(budgetItemService.getMaxDate()).thenReturn(LocalDateTime.now());
        when(budgetItemService.getLastCurrencyIdByDate(LocalDate.now())).thenReturn(CURRENCY.getId());
        when(budgetItemService.getSumPriceByDefaultCurrencyAndType(any())).thenReturn(1000.00);
        when(budgetItemService.getForSelectedPeriod(any(), any())).thenReturn(List.of(BUDGET_ITEM));
        when(joinRequestService.getNewJoinRequests()).thenReturn(List.of());

        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                .andExpect(view().name("menu"))
                .andExpect(status().isOk());
    }
}
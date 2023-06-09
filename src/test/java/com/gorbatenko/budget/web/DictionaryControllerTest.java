package com.gorbatenko.budget.web;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DictionaryControllerTest  extends AbstractWebControllerTest {

    private static final String CONTROLLER_PATH = "/dictionaries/";

    @Test
    void getDictionaries() throws Exception {
        String path = CONTROLLER_PATH;
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("pageName", "Справочники"))
                .andExpect(view().name("dictionaries/dictionaries"))
                .andExpect(status().isOk());
    }

    @Test
    void getDictionaryKinds() throws Exception {
        String path = CONTROLLER_PATH+"kinds";
        when(kindService.getAll()).thenReturn(List.of(KIND));
        when(budgetItemService.getAll()).thenReturn(List.of(BUDGET_ITEM));
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("pageName", "Виды приходов//расходов"))
                .andExpect(view().name("dictionaries/kinds/kinds"))
                .andExpect(status().isOk());
    }

    @Test
    void getDictionaryCurrencies() throws Exception {
        String path = CONTROLLER_PATH+"currencies";
        when(currencyService.getAll()).thenReturn(List.of(CURRENCY));
        when(budgetItemService.getAll()).thenReturn(List.of(BUDGET_ITEM));
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("pageName", "Валюты"))
                .andExpect(view().name("dictionaries/currencies/currencies"))
                .andExpect(status().isOk());
    }
}
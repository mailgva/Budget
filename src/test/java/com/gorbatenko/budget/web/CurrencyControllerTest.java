package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.to.CurrencyTo;
import com.gorbatenko.budget.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CurrencyControllerTest extends AbstractWebControllerTest{

    private static final String CONTROLLER_PATH = "/dictionaries/currencies/";

    private static final String ID = CURRENCY.getId();

    @Test
    void create() throws Exception {
        String path = CONTROLLER_PATH+"create";
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("pageName", "Создание"))
                .andExpect(view().name("dictionaries/currencies/edit"))
                .andExpect(status().isOk());
    }

    @Test
    void edit() throws Exception {
        String path = CONTROLLER_PATH+"edit/"+ID;
        when(currencyService.getById(ID)).thenReturn(BUDGET_ITEM.getCurrency());
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("currency", BUDGET_ITEM.getCurrency()))
                .andExpect(model().attribute("pageName", "Изменение"))
                .andExpect(view().name("dictionaries/currencies/edit"))
                .andExpect(status().isOk());
    }

    @Test
    void editCurrency() throws Exception {
        String path = CONTROLLER_PATH+"edit";
        CurrencyTo currencyTo = new CurrencyTo("NEW CURRENCY", false);
        currencyTo.setId(ID);

        Currency newCurrency = new Currency(currencyTo.getName(), currencyTo.isHidden());
        newCurrency.setId(ID);

        when(currencyService.getByName(currencyTo.getName())).thenReturn(null);
        when(currencyService.getById(ID)).thenReturn(CURRENCY);
        when(currencyService.save(any())).thenReturn(newCurrency);
        when(budgetItemService.getByCurrencyId(ID)).thenReturn(new ArrayList<>());
        when(regularOperationService.getByCurrencyId(ID)).thenReturn(new ArrayList<>());

        try (MockedStatic<SecurityUtil> utils = Mockito.mockStatic(SecurityUtil.class)) {
            utils.when(() -> SecurityUtil.getUserGroup()).thenReturn(TEST_USER.getId());

            mockMvc.perform(post(path).with(CSRF).params(PARAMS_CSRF_TOKEN)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("id", ID)
                            .param("name", currencyTo.getName())
                            .param("hidden", "False"))
                    //.andDo(print())
                    .andExpect(redirectedUrl("/dictionaries/currencies"));
        }
    }

    @Test
    void deleteCurrency() throws Exception {
        String path = CONTROLLER_PATH+ID;
        Currency defCurrency = new Currency("TEST", false);
        defCurrency.setId(UUID.randomUUID().toString());

        when(currencyService.getById(ID)).thenReturn(BUDGET_ITEM.getCurrency());
        when(budgetItemService.getByCurrencyId(ID)).thenReturn(List.of());
        when(regularOperationService.getByCurrencyId(ID)).thenReturn(List.of());

        try (MockedStatic<SecurityUtil> utils = Mockito.mockStatic(SecurityUtil.class)) {
            utils.when(() -> SecurityUtil.getCurrencyDefault()).thenReturn(defCurrency);
            mockMvc.perform(delete(path)).andExpect(status().isOk());
        }
    }
}
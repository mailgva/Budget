package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Every;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.to.RegularOperationTo;
import com.gorbatenko.budget.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RegularOperationControllerTest extends BaseWebControllerTest {

    private static final String CONTROLLER_PATH = "/regularoperations/";

    private static final RegularOperation OPERATION = new RegularOperation();
    static {
        OPERATION.setId(UUID.randomUUID());
        OPERATION.setCurrency(CURRENCY);
        OPERATION.setKind(KIND);
        OPERATION.setUser(TEST_USER);
        OPERATION.setUserGroup(TEST_USER.getId());
        OPERATION.setDescription("Test description");
        OPERATION.setEvery(Every.DAY);
    }

    private static final UUID ID = OPERATION.getId();

    @Test
    void getRegularOperations() throws Exception {
        String path = CONTROLLER_PATH;
        when(regularOperationService.getAll()).thenReturn(List.of(OPERATION));
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("operations", List.of(OPERATION)))
                .andExpect(model().attribute("pageName", "Регулярные операции"))
                .andExpect(view().name("regularoperations/operations"))
                .andExpect(status().isOk());
    }

    @Test
    void create() throws Exception {
        String path = CONTROLLER_PATH+"create";
        when(kindService.findAll()).thenReturn(List.of(KIND));
        when(currencyService.findAllVisible()).thenReturn(List.of(CURRENCY));

        try (MockedStatic<SecurityUtil> utils = Mockito.mockStatic(SecurityUtil.class)) {
            utils.when(SecurityUtil::getCurrencyDefault).thenReturn(CURRENCY);

            mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                    //.andDo(print())
                    .andExpect(model().attribute("pageName", "Создание"))
                    .andExpect(view().name("regularoperations/edit"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void deleteRegularOperation() throws Exception {
        String path = CONTROLLER_PATH+ID;
        when(regularOperationService.findById(ID)).thenReturn(OPERATION);
        mockMvc.perform(delete(path)).andExpect(status().isOk());
    }

    @Test
    void editCreateRegularOperation() throws Exception {
        String path = CONTROLLER_PATH;
        RegularOperationTo regularOperationTo = new RegularOperationTo();
        regularOperationTo.setId(OPERATION.getId());
        regularOperationTo.setEvery(Every.DAY);
        regularOperationTo.setCurrencyId(OPERATION.getCurrency().getId());
        regularOperationTo.setKindId(OPERATION.getKind().getId());
        regularOperationTo.setDescription("Modify description");
        regularOperationTo.setPrice(5000.00);

        when(regularOperationService.findById(ID)).thenReturn(OPERATION);
        try (MockedStatic<SecurityUtil> utils = Mockito.mockStatic(SecurityUtil.class)) {
            utils.when(() -> SecurityUtil.get()).thenReturn(AUTHORIZED_USER);

            when(regularOperationService.save(OPERATION)).thenReturn(OPERATION);
            mockMvc.perform(post(path).with(CSRF).params(PARAMS_CSRF_TOKEN)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("id", ID.toString())
                            .param("kindId", regularOperationTo.getKindId().toString())
                            .param("currencyId", regularOperationTo.getCurrencyId().toString())
                            .param("every", regularOperationTo.getEvery().name())
                            .param("description", regularOperationTo.getDescription())
                            .param("price", regularOperationTo.getPrice().toString())
                    )
                    //.andDo(print())
                    .andExpect(redirectedUrl("/regularoperations/"));
        }
    }

    @Test
    void edit() throws Exception {
        String path = CONTROLLER_PATH+"edit/"+ID;
        when(regularOperationService.findById(ID)).thenReturn(OPERATION);
        when(kindService.findAll()).thenReturn(List.of(KIND));
        when(currencyService.findAllVisible()).thenReturn(List.of(CURRENCY));
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("pageName", "Изменение"))
                .andExpect(view().name("regularoperations/edit"))
                .andExpect(status().isOk());
    }
}
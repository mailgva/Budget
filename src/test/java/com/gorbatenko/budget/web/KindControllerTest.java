package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.to.KindTo;
import com.gorbatenko.budget.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class KindControllerTest extends AbstractWebControllerTest {

    private static final String CONTROLLER_PATH = "/dictionaries/kinds/";

    private static final String ID = KIND.getId();

    @Test
    void create() throws Exception {
        String path = CONTROLLER_PATH+"create/PROFIT";
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("pageName", "Создание"))
                .andExpect(view().name("dictionaries/kinds/edit"))
                .andExpect(status().isOk());
    }

    @Test
    void edit() throws Exception {
        String path = CONTROLLER_PATH+"edit/"+ID;
        when(kindService.getById(ID)).thenReturn(KIND);
        mockMvc.perform(get(path).with(CSRF).params(PARAMS_CSRF_TOKEN))
                //.andDo(print())
                .andExpect(model().attribute("kind", KIND))
                .andExpect(model().attribute("pageName", "Изменение"))
                .andExpect(view().name("dictionaries/kinds/edit"))
                .andExpect(status().isOk());
    }

    @Test
    void editKind() throws Exception {
        String path = CONTROLLER_PATH+"edit";
        KindTo kindTo = new KindTo(Type.PROFIT, "NEW KIND", false);
        kindTo.setId(ID);

        Kind newKind = new Kind(kindTo.getType(), kindTo.getName(), kindTo.isHidden());
        newKind.setId(ID);

        when(kindService.getKindsByNameAndType(kindTo.getName(), kindTo.getType())).thenReturn(new ArrayList<>());
        when(kindService.getById(ID)).thenReturn(KIND);
        when(kindService.save(any())).thenReturn(newKind);
        when(budgetItemService.getByKindId(ID)).thenReturn(new ArrayList<>());
        when(regularOperationService.getByKindId(ID)).thenReturn(new ArrayList<>());

        try (MockedStatic<SecurityUtil> utils = Mockito.mockStatic(SecurityUtil.class)) {
            utils.when(() -> SecurityUtil.getUserGroup()).thenReturn(TEST_USER.getId());

            mockMvc.perform(post(path).with(CSRF).params(PARAMS_CSRF_TOKEN)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("id", ID)
                            .param("type", kindTo.getType().name())
                            .param("name", kindTo.getName())
                            .param("hidden", "False"))
                    //.andDo(print())
                    .andExpect(redirectedUrl("/dictionaries/kinds"));
        }
    }

    @Test
    void deleteKind() throws Exception{
        String path = CONTROLLER_PATH+ID;

        when(kindService.getById(ID)).thenReturn(KIND);
        when(budgetItemService.getByKindId(ID)).thenReturn(List.of());
        when(regularOperationService.getByKindId(ID)).thenReturn(List.of());

        mockMvc.perform(delete(path)).andExpect(status().isOk());
    }
}
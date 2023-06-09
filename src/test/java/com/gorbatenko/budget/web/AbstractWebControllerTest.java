package com.gorbatenko.budget.web;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.doc.User;
import com.gorbatenko.budget.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@SpringBootTest(webEnvironment = MOCK)
@AutoConfigureMockMvc(addFilters = false)
public class AbstractWebControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected CurrencyService currencyService;

    @MockBean
    protected KindService kindService;

    @MockBean
    protected BudgetItemService budgetItemService;

    @MockBean
    protected RegularOperationService regularOperationService;

    @MockBean
    protected UserService userService;

    @MockBean
    protected JoinRequestService joinRequestService;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final CsrfToken CSRF_TOKEN = new HttpSessionCsrfTokenRepository().generateToken(new MockHttpServletRequest());

    public static final SecurityMockMvcRequestPostProcessors.CsrfRequestPostProcessor CSRF = SecurityMockMvcRequestPostProcessors.csrf();

    public static final LinkedMultiValueMap<String, String> PARAMS_CSRF_TOKEN = new LinkedMultiValueMap<>();
    static {
        PARAMS_CSRF_TOKEN.add(CSRF_TOKEN.getParameterName(), CSRF_TOKEN.getToken());
    }

    public static final Currency CURRENCY = new Currency("VALUTA", false);
    static {
        CURRENCY.setId(UUID.randomUUID().toString());
    }

    public static final com.gorbatenko.budget.model.User TEST_USER = new com.gorbatenko.budget.model.User(
            UUID.randomUUID().toString(), "TEST USER", "user@user.com", "123456");
    static {
        TEST_USER.setGroup(TEST_USER.getId());
        TEST_USER.setCurrencyDefault(CURRENCY);
        CURRENCY.setUserGroup(TEST_USER.getId());
    }

    public static final User DOC_USER = new User(TEST_USER.getId(), TEST_USER.getName());

    public static final AuthorizedUser AUTHORIZED_USER = new AuthorizedUser(TEST_USER);

    public static final Kind KIND = new Kind(UUID.randomUUID().toString(), Type.PROFIT, "Test", false);

    public static final BudgetItem BUDGET_ITEM = new BudgetItem();
    static {
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 4, 0, 0);
        BUDGET_ITEM.setId(UUID.randomUUID().toString());
        BUDGET_ITEM.setUser(DOC_USER);
        BUDGET_ITEM.setKind(KIND);
        BUDGET_ITEM.setCurrency(CURRENCY);
        BUDGET_ITEM.setDate(dateTime);
        BUDGET_ITEM.setCreateDateTime(dateTime);
        BUDGET_ITEM.setDescription("TEST Description");
        BUDGET_ITEM.setPrice(1000.00);
    }
}

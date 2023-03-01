package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.to.CurrencyTo;
import com.gorbatenko.budget.util.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;

import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;
import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/currencies/")
public class CurrencyController extends AbstractWebController {

    @GetMapping("create")
    public String create(Model model) {
        Currency currency = new Currency();
        model.addAttribute("currency", currency);
        model.addAttribute("pageName", "Создание");
        return "/dictionaries/currencies/edit";
    }

    @GetMapping("edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        model.addAttribute("currency", currencyRepository.getById(id));
        model.addAttribute("pageName", "Изменение");
        return "/dictionaries/currencies/edit";
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Response> delete(@PathVariable("id") String id) {
        String errorMessage = "Невозможно удалить валюту, так как она %s";
        Currency currency = currencyRepository.getById(id);

        if (currency == null) {
            String message = String.format(errorMessage, "не найдена");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (!budgetItemRepository.getByCurrencyId(id).isEmpty()) {
            String message = String.format(errorMessage, "используется в бюджете");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (!regularOperationRepository.getByCurrencyId(id).isEmpty()) {
            String message = String.format(errorMessage, "используется в регулярных операциях");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (getCurrencyDefault().getId().equals(id)) {
            String message = String.format(errorMessage, "установлена как валюта по умолчанию");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        currencyRepository.deleteById(id);
        return ResponseEntity.ok(new Response(200, null));
    }

    @Transactional
    @PostMapping(value = "edit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String editCurrency(@Valid @RequestBody CurrencyTo currencyTo, RedirectAttributes rm) {
        if (currencyTo.getId().isEmpty()) {
            currencyTo.setId(null);
        }

        Currency currency = new Currency(currencyTo.getName(), currencyTo.isHidden());
        currency.setId(currencyTo.getId());
        currency.setUserGroup(getUserGroup());

        Currency currencyByName = currencyRepository.getByName(currencyTo.getName());

        if (Objects.deepEquals(currency, currencyByName)) {
            return "redirect:/dictionaries/currencies";
        }

        if (currencyByName != null &&
                (currencyTo.getId() != null && !currencyTo.getId().equals(currencyByName.getId()))) {
            rm.addFlashAttribute("error", "Валюта с наименованием '" + currencyTo.getName() + "'" +
                    " уже используется!");
            return String.format("redirect:/dictionaries/currencies/edit/%s", currencyTo.getId());
        }

        if (currencyTo.getId() != null) {
            if (currencyRepository.getById(currencyTo.getId()) == null) {
                rm.addFlashAttribute("error", "Невозможно изменить, валюта не найдена!");
                return "redirect:/dictionaries/kinds/currencies/";
            }
        }

        currency = currencyRepository.save(currency);

        List<BudgetItem> budgetItems = budgetItemRepository.getByCurrencyId(currency.getId());
        for (BudgetItem budgetItem : budgetItems) {
            budgetItem.setCurrency(currency);
            budgetItemRepository.save(budgetItem);
        }

        List<RegularOperation> operations = regularOperationRepository.getByCurrencyId(currency.getId());
        for (RegularOperation operation : operations) {
            operation.setCurrency(currency);
            regularOperationRepository.save(operation);
        }

        return "redirect:/dictionaries/currencies";
    }

}

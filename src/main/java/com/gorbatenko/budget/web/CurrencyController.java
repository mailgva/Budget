package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.to.CurrencyTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/currencies/")
public class CurrencyController extends AbstractWebController {

    @GetMapping("/create")
    public String create(Model model) {
        Currency currency = new Currency();
        model.addAttribute("currency", currency);

        model.addAttribute("pageName", "Создание");

        return "/dictionaries/currencies/edit";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, @RequestParam(name = "error", defaultValue = "") String error, Model model) {
        if (!error.isEmpty()) {
            model.addAttribute("error", error);
        }
        model.addAttribute("currency", currencyRepository.getById(id));

        model.addAttribute("pageName", "Изменение");

        return "/dictionaries/currencies/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, RedirectAttributes rm) {
        String errorMessage = "Невозможно удалить статью, так как она $s";

        Currency currency = currencyRepository.getById(id);

        if (currency == null) {
            rm.addFlashAttribute("error", String.format(errorMessage, "не найдена"));
            return String.format("redirect:/dictionaries/kinds/edit/%s", id);
        }

        if (!budgetRepository.getByCurrencyId(id).isEmpty()) {
            rm.addFlashAttribute("error", String.format(errorMessage, "используется в бюджете"));
            return String.format("redirect:/dictionaries/currencies/edit/%s", id);
        }

        if (!regularOperationRepository.getByCurrencyId(id).isEmpty()) {
            rm.addFlashAttribute("error", String.format(errorMessage, "используется в регулярных операциях"));
            return String.format("redirect:/dictionaries/currencies/edit/%s", id);
        }

        if (getCurrencyDefault().getId().equals(id)) {
            rm.addFlashAttribute("error", "Невозможно удалить валюту, валюта [" + currency.getName() + "] установлена как валюта по умолчанию!");
            return String.format("redirect:/dictionaries/currencies/edit/%s", id);
        }

        currencyRepository.deleteById(id);
        return "redirect:/dictionaries/currencies";
    }

    @PostMapping("/edit")
    public String editCurrency(@Valid @ModelAttribute CurrencyTo currencyTo, RedirectAttributes rm) {
        if (currencyTo.getId().isEmpty()) {
            currencyTo.setId(null);
        }
        if (currencyRepository.getByName(currencyTo.getName()) != null) {
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

        Currency currency = new Currency(currencyTo.getName());
        currency.setId(currencyTo.getId());
        currency = currencyRepository.save(currency);

        List<Budget> budgets = budgetRepository.getByCurrencyId(currency.getId());
        for (Budget budget : budgets) {
            budget.setCurrency(currency);
            budgetRepository.save(budget);
        }

        List<RegularOperation> operations = regularOperationRepository.getByCurrencyId(currency.getId());
        for (RegularOperation operation : operations) {
            operation.setCurrency(currency);
            regularOperationRepository.save(operation);
        }

        return "redirect:/dictionaries/currencies";
    }

}

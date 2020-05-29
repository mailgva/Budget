package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.to.CurrencyTo;
import com.gorbatenko.budget.to.KindTo;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

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
  public String edit(@PathVariable("id") String id, @RequestParam(name="error", defaultValue = "") String error, Model model) {
    if(!error.isEmpty()) {
      model.addAttribute("error", error);
    }
    model.addAttribute("currency", currencyRepository.findById(id).get());

    model.addAttribute("pageName", "Изменение");

    return "/dictionaries/currencies/edit";
  }

  @GetMapping("/delete/{id}")
  public String delete(@PathVariable("id") String id, RedirectAttributes rm) {
    User user = SecurityUtil.get().getUser();
    Currency currency = currencyRepository.findByUserGroupAndId(user.getGroup(), id);
    if (budgetRepository.countByUser_GroupAndCurrency(user.getGroup(), currency) > 0) {
      rm.addFlashAttribute("error", "Невозможно удалить валюту, так как она уже используется");
      return String.format("redirect:/dictionaries/currencies/edit/%s", id);
    }

    if (user.getCurrencyDefault().getId().equals(id)) {
      rm.addFlashAttribute("error", "Невозможно удалить валюту, валюта [" + currency.getName() + "] установлена как валюта по умолчанию!");
      return String.format("redirect:/dictionaries/currencies/edit/%s", id);
    }

    currencyRepository.deleteById(id);
    return "redirect:/dictionaries/currencies";
  }

  @PostMapping("/edit")
  public String editNewDicCurrency(@Valid @ModelAttribute CurrencyTo currencyTo, RedirectAttributes rm) {
    User user = SecurityUtil.get().getUser();
    if(currencyTo.getId().isEmpty()) {
      currencyTo.setId(null);
    }
    Currency check = currencyRepository.findByUserGroupAndNameIgnoreCase(user.getGroup(), currencyTo.getName());
    if(check != null) {
      rm.addFlashAttribute("error", "Валюта с наименованием '" + currencyTo.getName() + "'" +
              " уже используется!");
      return String.format("redirect:/dictionaries/currencies/edit/%s", currencyTo.getId());
    }

    Currency currencyOld = currencyRepository.findByUserGroupAndId(user.getGroup(), currencyTo.getId());
    Currency currency = createCurrencyFromCurrencyTo(currencyTo);
    currency.setId(currencyTo.getId());
    currency = currencyRepository.save(currency);
    List<Budget> budgets = budgetRepository.getBudgetByCurrencyAndUser_Group(currencyOld, user.getGroup());
    for(Budget budget : budgets) {
      budget.setCurrency(currency);
      budgetRepository.save(budget);
    }
    return "redirect:/dictionaries/currencies";
  }

  private Currency createCurrencyFromCurrencyTo(CurrencyTo currencyTo) {
    User user = SecurityUtil.get().getUser();
    Currency currency = new Currency(currencyTo.getName(), user.getGroup());
    return currency;
  }

}

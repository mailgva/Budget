package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.to.RegularOperationTo;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;
import static com.gorbatenko.budget.web.BudgetController.getSumTimezoneOffsetMinutes;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/regularoperations/")
public class RegularOperationController extends AbstractWebController{

    @GetMapping("/")
    String getRegularOperations(Model model) {
        model.addAttribute("operations", regularOperationRepository.getAll());
        model.addAttribute("pageName", "Регулярные операции");
        return "/regularoperations/operations";
    }

    @GetMapping("/create")
    public String create(Model model) {
        RegularOperation operation = new RegularOperation();
        List<Every> everies = Arrays.stream(Every.values()).sorted(Comparator.comparingInt(Every::getPosit)).collect(Collectors.toList());
        List<Kind> kinds = kindRepository.getAll();
        List<Currency> currencies = currencyRepository.getAll();

        operation.setCurrency(getCurrencyDefault());

        model.addAttribute("operation", operation);
        model.addAttribute("everies", everies);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("pageName", "Создание");

        return "/regularoperations/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, RedirectAttributes rm) {
        RegularOperation operation = regularOperationRepository.getById(id);
        if (operation == null) {
            rm.addFlashAttribute("error", "Невозможно удалить операцию, так как она не найдена");
            return String.format("redirect:/regularoperations/edit/%s", id);
        }

        regularOperationRepository.deleteById(id);

        return "redirect:/regularoperations/";
    }

    @PostMapping("/")
    public String editCreateRegularOperation(@Valid @ModelAttribute RegularOperationTo regularOperationTo,
                                 @RequestParam(name="referer", defaultValue = "") String referer,
                                 HttpServletRequest request,
                                 RedirectAttributes rm) {

        if(regularOperationTo.getId().isEmpty()) {
            regularOperationTo.setId(null);
        } else {
            if(regularOperationRepository.getById(regularOperationTo.getId()) == null) {
                rm.addFlashAttribute("error", "Невозможно изменить операцию, так как она не найдена");
                return String.format("redirect:/regularoperations/edit/%s", regularOperationTo.getId());
            }
        }

        RegularOperation regularOperation = createRegularOperationFromTo(regularOperationTo, request);
        regularOperation.setId(regularOperationTo.getId());
        regularOperationRepository.save(regularOperation);

        rm.addFlashAttribute("regularOperationId", regularOperation.getId());
        return (referer.isEmpty() ? "redirect:/regularoperations/" : "redirect:" + referer);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, @RequestParam(name="error", defaultValue = "") String error, Model model) {
        if(!error.isEmpty()) {
            model.addAttribute("error", error);
        }

        List<Every> everies = Arrays.stream(Every.values()).sorted(Comparator.comparingInt(Every::getPosit)).collect(Collectors.toList());
        List<Kind> kinds = kindRepository.getAll();
        List<Currency> currencies = currencyRepository.getAll();

        model.addAttribute("operation", regularOperationRepository.getById(id));

        model.addAttribute("everies", everies);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);

        model.addAttribute("pageName", "Изменение");

        return "/regularoperations/edit";
    }

    private RegularOperation createRegularOperationFromTo(RegularOperationTo regularOperationTo, HttpServletRequest request) {
        User user = SecurityUtil.get().getUser();
        int countTimezoneOffsetMinutes = getSumTimezoneOffsetMinutes(request);
        Kind kind = kindRepository.getById(regularOperationTo.getKindId());
        Currency currency = currencyRepository.getById(regularOperationTo.getCurrencyId());
        return new RegularOperation(
                user,
                user.getGroup(),
                countTimezoneOffsetMinutes,
                regularOperationTo.getEvery(),
                regularOperationTo.getDayOfMonth(),
                kind,
                regularOperationTo.getDescription(),
                regularOperationTo.getPrice(),
                currency);
    }
}

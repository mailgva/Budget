package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.to.CurrencyTo;
import com.gorbatenko.budget.to.KindTo;
import com.gorbatenko.budget.to.RegularOperationTo;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/regularoperations/")
public class RegularOperationController extends AbstractWebController{

    @GetMapping("/")
    String getRegularOperations(Model model) {
        User user = SecurityUtil.get().getUser();
        model.addAttribute("operations", regularOperationRepository.getByUserGroupOrderByEveryPositAsc(user.getGroup()));
        model.addAttribute("pageName", "Регулярные операции");
        return "/regularoperations/operations";
    }

    @GetMapping("/create")
    public String create(Model model) {
        User user = SecurityUtil.get().getUser();
        RegularOperation operation = new RegularOperation();
        List<Every> everies = Arrays.stream(Every.values()).sorted(Comparator.comparingInt(Every::getPosit)).collect(Collectors.toList());
        List<Kind> kinds = kindRepository.getKindByUserGroupOrderByTypeAscNameAsc(user.getGroup());
        List<Currency> currencies = currencyRepository.getCurrencyByUserGroupOrderByNameAsc(user.getGroup());

        operation.setCurrency(user.getCurrencyDefault());

        model.addAttribute("operation", operation);
        model.addAttribute("everies", everies);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("pageName", "Создание");

        return "/regularoperations/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();
        RegularOperation operation = regularOperationRepository.getRegularOperationByUserGroupAndId(user.getGroup(), id);
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
                                 RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();

        if(regularOperationTo.getId().isEmpty()) {
            regularOperationTo.setId(null);
        } else {
            if(regularOperationRepository.getRegularOperationByUserGroupAndId(user.getGroup(), regularOperationTo.getId()) == null) {
                rm.addFlashAttribute("error", "Невозможно удалить операцию, так как она не найдена");
                return String.format("redirect:/regularoperations/edit/%s", regularOperationTo.getId());
            }
        }

        RegularOperation regularOperation = createRegularOperationFromTo(regularOperationTo);
        regularOperation.setId(regularOperationTo.getId());
        regularOperationRepository.save(regularOperation);

        rm.addFlashAttribute("regularOperationId", regularOperation.getId());
        return (referer.isEmpty() ? "redirect:/regularoperations/" : "redirect:" + referer);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, @RequestParam(name="error", defaultValue = "") String error, Model model) {
        User user = SecurityUtil.get().getUser();
        if(!error.isEmpty()) {
            model.addAttribute("error", error);
        }

        List<Every> everies = Arrays.stream(Every.values()).sorted(Comparator.comparingInt(Every::getPosit)).collect(Collectors.toList());
        List<Kind> kinds = kindRepository.getKindByUserGroupOrderByTypeAscNameAsc(user.getGroup());
        List<Currency> currencies = currencyRepository.getCurrencyByUserGroupOrderByNameAsc(user.getGroup());

        model.addAttribute("operation", regularOperationRepository.findById(id).get());

        model.addAttribute("everies", everies);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);

        model.addAttribute("pageName", "Изменение");

        return "/regularoperations/edit";
    }

    private RegularOperation createRegularOperationFromTo(RegularOperationTo regularOperationTo) {
        User user = SecurityUtil.get().getUser();
        Kind kind = kindRepository.getKindByUserGroupAndId(user.getGroup(), regularOperationTo.getKindId());
        Currency currency = currencyRepository.getCurrencyByUserGroupAndId(user.getGroup(), regularOperationTo.getCurrencyId());
        RegularOperation regularOperation = new RegularOperation(
                user,
                user.getGroup(),
                regularOperationTo.getEvery(),
                regularOperationTo.getDayOfMonth(),
                kind,
                regularOperationTo.getDescription(),
                regularOperationTo.getPrice(),
                currency);
        return regularOperation;
    }
}

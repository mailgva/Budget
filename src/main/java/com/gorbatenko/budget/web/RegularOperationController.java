package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.to.RegularOperationTo;
import com.gorbatenko.budget.util.Response;
import com.gorbatenko.budget.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;
import static com.gorbatenko.budget.web.BudgetItemController.getSumTimeZoneOffsetMinutes;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/regularoperations/")
public class RegularOperationController extends AbstractWebController{

    @GetMapping
    String getRegularOperations(Model model) {
        model.addAttribute("operations", regularOperationService.getAll());
        model.addAttribute("pageName", "Регулярные операции");
        return "regularoperations/operations";
    }

    @GetMapping("create")
    public String create(Model model) {
        RegularOperation operation = new RegularOperation();
        List<Every> everies = Arrays.stream(Every.values()).sorted(Comparator.comparingInt(Every::getPosit)).collect(Collectors.toList());
        List<Kind> kinds = kindService.getAll();
        List<Currency> currencies = currencyService.getVisibled();

        operation.setCurrency(getCurrencyDefault());

        model.addAttribute("operation", operation);
        model.addAttribute("everies", everies);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("pageName", "Создание");

        return "regularoperations/edit";
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Response> delete(@PathVariable("id") String id) {
        RegularOperation operation = regularOperationService.getById(id);
        if (operation == null) {
            String message = "Невозможно удалить операцию, так как она не найдена";
            return ResponseEntity.badRequest().body(new Response(400, message));
        }
        regularOperationService.deleteById(id);
        return ResponseEntity.ok(new Response(200, null));
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String editCreateRegularOperation(@Valid @RequestBody RegularOperationTo regularOperationTo,
                                 @RequestParam(name="referer", defaultValue = "") String referer, TimeZone tz,
                                 RedirectAttributes rm) {

        if(regularOperationTo.getId().isEmpty()) {
            regularOperationTo.setId(null);
        } else {
            if(regularOperationService.getById(regularOperationTo.getId()) == null) {
                rm.addFlashAttribute("error", "Невозможно изменить операцию, так как она не найдена");
                return String.format("redirect:/regularoperations/edit/%s", regularOperationTo.getId());
            }
        }

        RegularOperation regularOperation = createRegularOperationFromTo(regularOperationTo, tz);
        regularOperation.setId(regularOperationTo.getId());
        regularOperationService.save(regularOperation);

        rm.addFlashAttribute("regularOperationId", regularOperation.getId());
        return (referer.isEmpty() ? "redirect:/regularoperations/" : "redirect:" + referer);
    }

    @GetMapping("edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) throws Exception {
        RegularOperation regularOperation = regularOperationService.getById(id);
        if (regularOperation == null) {
            throw new Exception("Запись не найдена!");
        }
        List<Every> everies = Arrays.stream(Every.values()).sorted(Comparator.comparingInt(Every::getPosit)).collect(Collectors.toList());
        List<Kind> kinds = kindService.getAll();
        List<Currency> currencies = currencyService.getVisibled();

        model.addAttribute("operation", regularOperation);
        model.addAttribute("everies", everies);
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("pageName", "Изменение");

        return "regularoperations/edit";
    }

    private RegularOperation createRegularOperationFromTo(RegularOperationTo regularOperationTo, TimeZone tz) {
        User user = SecurityUtil.get().getUser();
        int countTimeZoneOffsetMinutes = getSumTimeZoneOffsetMinutes(tz);
        Kind kind = kindService.getById(regularOperationTo.getKindId());
        Currency currency = currencyService.getById(regularOperationTo.getCurrencyId());
        return new RegularOperation(
                user,
                user.getGroup(),
                countTimeZoneOffsetMinutes,
                regularOperationTo.getEvery(),
                regularOperationTo.getDayOfMonth(),
                kind,
                regularOperationTo.getDescription(),
                regularOperationTo.getPrice(),
                currency);
    }
}

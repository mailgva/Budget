package com.gorbatenko.budget.web;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Every;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.service.BudgetItemService;
import com.gorbatenko.budget.service.CurrencyService;
import com.gorbatenko.budget.service.KindService;
import com.gorbatenko.budget.service.RegularOperationService;
import com.gorbatenko.budget.to.KindTo;
import com.gorbatenko.budget.to.RegularOperationTo;
import com.gorbatenko.budget.util.Response;
import com.gorbatenko.budget.util.SecurityUtil;
import static com.gorbatenko.budget.util.SecurityUtil.getCurrencyDefault;

import jakarta.validation.Valid;
import tools.jackson.databind.ObjectMapper;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/regularoperations/")
public class RegularOperationController extends BaseWebController {

    private final RegularOperationService regularOperationService;

    public RegularOperationController(CurrencyService currencyService, KindService kindService,
                                      BudgetItemService budgetItemService, RegularOperationService regularOperationService) {
        super(currencyService, kindService, budgetItemService);
        this.regularOperationService = regularOperationService;
    }

    @GetMapping
    public String getRegularOperations(Model model) {
        model.addAttribute("operations", regularOperationService.getAll());
        model.addAttribute("pageName", "Регулярные операции");
        return "regularoperations/operations";
    }

    @GetMapping("create")
    public String create(Model model) {
        RegularOperation operation = new RegularOperation();
        List<Every> everies = Arrays.stream(Every.values()).sorted(Comparator.comparingInt(Every::getPosit)).collect(Collectors.toList());
        List<Kind> kinds = kindService.findAll();
        TreeMap<Type, List<KindTo>> mapKind = new TreeMap(
                kinds.stream()
                        .filter(kind -> !kind.getHidden())
                        .map(kind -> new KindTo(kind.getId(), kind.getType(), kind.getName(), true))
                        .toList().stream()
                .collect(groupingBy(KindTo::getType)));
        List<Currency> currencies = currencyService.findAllVisible();

        operation.setCurrency(getCurrencyDefault());

        model.addAttribute("operation", operation);
        model.addAttribute("editKindId", "''");
        model.addAttribute("everies", everies);
        model.addAttribute("types", List.of(Type.PROFIT, Type.SPENDING));
        model.addAttribute("mapKind", toJson(mapKind));
        model.addAttribute("currencies", currencies);
        model.addAttribute("pageName", "Создание");

        return "regularoperations/edit";
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Response> delete(@PathVariable("id") UUID id) {
        RegularOperation operation = regularOperationService.findById(id);
        if (operation == null) {
            String message = "Невозможно удалить операцию, так как она не найдена";
            return ResponseEntity.badRequest().body(new Response(400, message));
        }
        regularOperationService.deleteById(id);
        return ResponseEntity.ok(new Response(200, null));
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String editCreateRegularOperation(@Valid @RequestBody RegularOperationTo regularOperationTo,
                                 @RequestParam(name="referer", defaultValue = "") String referer,
                                 RedirectAttributes rm) {

        if(regularOperationTo.getId() == null) {
            regularOperationTo.setId(null);
        } else {
            if(regularOperationService.findById(regularOperationTo.getId()) == null) {
                rm.addFlashAttribute("error", "Невозможно изменить операцию, так как она не найдена");
                return String.format("redirect:/regularoperations/edit/%s", regularOperationTo.getId());
            }
        }

        RegularOperation regularOperation = createRegularOperationFromTo(regularOperationTo);
        regularOperation.setId(regularOperationTo.getId());
        regularOperationService.save(regularOperation);

        rm.addFlashAttribute("regularOperationId", regularOperation.getId());
        return (referer.isEmpty() ? "redirect:/regularoperations/" : "redirect:" + referer);
    }

    @GetMapping("edit/{id}")
    public String edit(@PathVariable("id") UUID id, Model model) throws Exception {
        RegularOperation regularOperation = regularOperationService.findById(id);
        if (regularOperation == null) {
            throw new Exception("Запись не найдена!");
        }
        List<Every> everies = Arrays.stream(Every.values()).sorted(Comparator.comparingInt(Every::getPosit)).collect(Collectors.toList());
        List<Kind> kinds = kindService.findAll();
        TreeMap<Type, List<KindTo>> mapKind = new TreeMap(
                kinds.stream()
                        .filter(kind -> !kind.getHidden())
                        .map(kind -> new KindTo(kind.getId(), kind.getType(), kind.getName(), true))
                        .toList().stream()
                        .collect(groupingBy(KindTo::getType)));        List<Currency> currencies = currencyService.findAllVisible();

        model.addAttribute("operation", regularOperation);
        model.addAttribute("editKindId", "'" + regularOperation.getKind().getId() + "'");
        model.addAttribute("everies", everies);
        model.addAttribute("types", List.of(Type.PROFIT, Type.SPENDING));
        model.addAttribute("mapKind", toJson(mapKind));
        model.addAttribute("kinds", kinds);
        model.addAttribute("currencies", currencies);
        model.addAttribute("pageName", "Изменение");

        return "regularoperations/edit";
    }

    private RegularOperation createRegularOperationFromTo(RegularOperationTo regularOperationTo) {
        User user = SecurityUtil.get().getUser();
        Kind kind = kindService.findById(regularOperationTo.getKindId());
        Currency currency = currencyService.findById(regularOperationTo.getCurrencyId());
        return new RegularOperation(
                user,
                user.getUserGroup(),
                regularOperationTo.getEvery(),
                regularOperationTo.getDayOfMonth(),
                kind,
                regularOperationTo.getDescription(),
                regularOperationTo.getPrice(),
                currency);
    }

    private static String toJson(TreeMap<Type, List<KindTo>> mapKind){
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(mapKind);
    }
}

package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.service.*;
import com.gorbatenko.budget.to.KindTo;
import com.gorbatenko.budget.util.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.gorbatenko.budget.model.Kind.EXCHANGE_NAME;
import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;
import static com.gorbatenko.budget.util.Utils.equalsUUID;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/kinds/")
public class KindController extends BaseWebController {

    private final RegularOperationService regularOperationService;

    public KindController(CurrencyService currencyService, KindService kindService, BudgetItemService budgetItemService,
                          RegularOperationService regularOperationService) {
        super(currencyService, kindService, budgetItemService);
        this.regularOperationService = regularOperationService;
    }

    @GetMapping("create/{type}")
    public String create(@PathVariable("type") String type, Model model, HttpServletRequest request) {
        Kind kind = new Kind();
        kind.setType(Type.valueOf(type.toUpperCase()));
        String referer = request.getHeader("referer");
        model.addAttribute("kind", kind);
        model.addAttribute("referer", referer);
        model.addAttribute("pageName", "Создание");
        return "dictionaries/kinds/edit";
    }

    @GetMapping("edit/{id}")
    public String edit(@PathVariable("id") UUID id, Model model) throws Exception {
        Kind kind = kindService.findById(id);
        if (kind == null) {
            throw new Exception("Запись не найдена!");
        }
        model.addAttribute("kind", kind);
        model.addAttribute("pageName", "Изменение");
        return "dictionaries/kinds/edit";
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Response> delete(@PathVariable("id") UUID id) {
        String errorMessage = "Невозможно удалить статью, так как она %s";
        Kind kind = kindService.findById(id);

        if (kind == null) {
            String message = String.format(errorMessage, "не найдена");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (kind.getName().equals(EXCHANGE_NAME)) {
            String message = String.format(errorMessage, "является системной");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (!budgetItemService.findByKindId(id).isEmpty()) {
            String message = String.format(errorMessage, "используется в бюджете");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (!regularOperationService.findByKindId(id).isEmpty()) {
            String message = String.format(errorMessage, "используется в регулярных операциях");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        kindService.deleteById(id);
        return ResponseEntity.ok(new Response(200, null));
    }

    @Transactional
    @PostMapping(value = "edit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String editKind(@Valid @RequestBody KindTo kindTo,
                                 @RequestParam(name="referer", defaultValue = "") String referer,
                                 RedirectAttributes rm) {
        Kind kind = createKindFromKindTo(kindTo);
        kind.setUserGroup(getUserGroup());

        Kind kindExists = kindService.findByNameAndType(kindTo.getType(), kindTo.getName());
        if(kindExists != null) {
            if (Objects.deepEquals(kind, kindExists)) {
                return (referer.isEmpty() ? "redirect:/dictionaries/kinds" : "redirect:" + referer);
            }
            if (kindTo.getId() != null && !equalsUUID(kindTo.getId(), kindExists.getId())) {
                rm.addFlashAttribute("error", "Статья с наименованием '" + kindTo.getName() + "'" +
                        " уже используется в '" + kindTo.getType().getValue() + "'!");
                if (referer.isEmpty()) {
                    return String.format("redirect:/dictionaries/kinds/edit/%s", kindTo.getId());
                } else {
                    return referer;
                }
            }
        }

        if (kindTo.getId() != null) {
            Kind kindById = kindService.findById(kindTo.getId());
            String message = "";
            if (kindById == null) {
                message = "Невозможно изменить, статья не найдена!";
            }
            if (EXCHANGE_NAME.equals(kindById.getName()) && !EXCHANGE_NAME.equals(kindTo.getName())) {
                message = "Невозможно изменить, статья является системной!";
            }
            if (!message.isEmpty()) {
                rm.addFlashAttribute("error", message);
                return "redirect:/dictionaries/kinds/";
            }
        }

        kind = kindService.save(kind);
        rm.addFlashAttribute("kindId", kind.getId());

        List<BudgetItem> budgetItems = budgetItemService.findByKindId(kind.getId());
        for(BudgetItem budgetItem : budgetItems) {
            budgetItem.setKind(kind);
            budgetItemService.save(budgetItem);
        }

        List<RegularOperation> operations = regularOperationService.findByKindId(kind.getId());
        for(RegularOperation operation : operations) {
            operation.setKind(kind);
            regularOperationService.save(operation);
        }

        return (referer.isEmpty() ? "redirect:/dictionaries/kinds" : "redirect:" + referer);
    }

    private Kind createKindFromKindTo(KindTo kindTo) {
        return new Kind(kindTo.getId(), kindTo.getType(), kindTo.getName(), kindTo.getHidden());
    }
}

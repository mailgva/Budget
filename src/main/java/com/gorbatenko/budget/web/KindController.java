package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.model.Type;
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

import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/kinds/")
public class KindController extends AbstractWebController{

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
    public String edit(@PathVariable("id") String id, Model model) throws Exception {
        Kind kind = kindService.getById(id);
        if (kind == null) {
            throw new Exception("Запись не найдена!");
        }
        model.addAttribute("kind", kind);
        model.addAttribute("pageName", "Изменение");
        return "dictionaries/kinds/edit";
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Response> delete(@PathVariable("id") String id) {
        String errorMessage = "Невозможно удалить статью, так как она %s";
        Kind kind = kindService.getById(id);
        if (kind == null) {
            String message = String.format(errorMessage, "не найдена");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (!budgetItemService.getByKindId(id).isEmpty()) {
            String message = String.format(errorMessage, "используется в бюджете");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (!regularOperationService.getByKindId(id).isEmpty()) {
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
        if(kindTo.getId().isEmpty()) {
            kindTo.setId(null);
        }
        Kind kind = createKindFromKindTo(kindTo);
        kind.setUserGroup(getUserGroup());

        List<Kind> filteredData = kindService.getKindsByNameAndType(kindTo.getName(), kindTo.getType());
        if(!filteredData.isEmpty()) {
            Kind firstKind = filteredData.get(0);
            if (Objects.deepEquals(kind, firstKind)) {
                return (referer.isEmpty() ? "redirect:/dictionaries/kinds" : "redirect:" + referer);
            }
            if (kindTo.getId() != null && !kindTo.getId().equals(firstKind.getId())) {
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
            if (kindService.getById(kindTo.getId()) == null) {
                rm.addFlashAttribute("error", "Невозможно изменить, статья не найдена!");
                return "redirect:/dictionaries/kinds/";
            }
        }

        kind = kindService.save(kind);
        rm.addFlashAttribute("kindId", kind.getId());

        List<BudgetItem> budgetItems = budgetItemService.getByKindId(kind.getId());
        for(BudgetItem budgetItem : budgetItems) {
            budgetItem.setKind(kind);
            budgetItemService.save(budgetItem);
        }

        List<RegularOperation> operations = regularOperationService.getByKindId(kind.getId());
        for(RegularOperation operation : operations) {
            operation.setKind(kind);
            regularOperationService.save(operation);
        }

        return (referer.isEmpty() ? "redirect:/dictionaries/kinds" : "redirect:" + referer);
    }

    private Kind createKindFromKindTo(KindTo kindTo) {
        return new Kind(kindTo.getId(), kindTo.getType(), kindTo.getName(), kindTo.isHidden());
    }
}

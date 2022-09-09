package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.to.KindTo;
import com.gorbatenko.budget.util.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static org.apache.logging.log4j.ThreadContext.isEmpty;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/kinds/")
public class KindController extends AbstractWebController{

    @GetMapping("/create/{type}")
    public String create(@PathVariable("type") String type, Model model, HttpServletRequest request) {
        Kind kind = new Kind();
        kind.setType(Type.valueOf(type.toUpperCase()));
        String referer = request.getHeader("referer");
        model.addAttribute("kind", kind);
        model.addAttribute("referer", referer);
        model.addAttribute("pageName", "Создание");
        return "/dictionaries/kinds/edit";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        model.addAttribute("kind", kindRepository.getById(id));
        model.addAttribute("pageName", "Изменение");
        return "/dictionaries/kinds/edit";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> delete(@PathVariable("id") String id) {
        String errorMessage = "Невозможно удалить статью, так как она %s";
        Kind kind = kindRepository.getById(id);
        if (kind == null) {
            String message = String.format(errorMessage, "не найдена");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (!budgetRepository.getByKindId(id).isEmpty()) {
            String message = String.format(errorMessage, "используется в бюджете");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        if (!regularOperationRepository.getByKindId(id).isEmpty()) {
            String message = String.format(errorMessage, "используется в регулярных операциях");
            return ResponseEntity.badRequest().body(new Response(400, message));
        }

        kindRepository.deleteById(id);
        return ResponseEntity.ok(new Response(200, null));
    }

    @Transactional
    @PostMapping("/edit")
    public String editKind(@Valid @ModelAttribute KindTo kindTo,
                                 @RequestParam(name="referer", defaultValue = "") String referer,
                                 RedirectAttributes rm) {
        if(kindTo.getId().isEmpty()) {
            kindTo.setId(null);
        }

        List<Kind> filteredData = kindRepository.getFilteredData(null, kindTo.getName(), kindTo.getType(), null);
        if(!filteredData.isEmpty()) {
            Kind kind = filteredData.get(0);
            if (kindTo.getId() != null && !kindTo.getId().equals(kind.getId())) {
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
            if (kindRepository.getById(kindTo.getId()) == null) {
                rm.addFlashAttribute("error", "Невозможно изменить, статья не найдена!");
                return "redirect:/dictionaries/kinds/";
            }
        }

        Kind kind = createKindFromKindTo(kindTo);
        kind.setId(kindTo.getId());
        kind.setHidden(kindTo.isHidden());
        kind = kindRepository.save(kind);

        List<Budget> budgets = budgetRepository.getByKindId(kind.getId());
        for(Budget budget : budgets) {
            budget.setKind(kind);
            budgetRepository.save(budget);
        }

        List<RegularOperation> operations = regularOperationRepository.getByKindId(kind.getId());
        for(RegularOperation operation : operations) {
            operation.setKind(kind);
            regularOperationRepository.save(operation);
        }

        rm.addFlashAttribute("kindId", kind.getId());
        return (referer.isEmpty() ? "redirect:/dictionaries/kinds" : "redirect:" + referer);
    }

    private Kind createKindFromKindTo(KindTo kindTo) {
        return new Kind(kindTo.getType(), kindTo.getName());
    }
}

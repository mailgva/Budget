package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.to.KindTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

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
    public String edit(@PathVariable("id") String id, @RequestParam(name="error", defaultValue = "") String error, Model model) {
        if(!error.isEmpty()) {
            model.addAttribute("error", error);
        }
        model.addAttribute("kind", kindRepository.getById(id));
        model.addAttribute("pageName", "Изменение");
        return "/dictionaries/kinds/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, RedirectAttributes rm) {
        String errorMessage = "Невозможно удалить статью, так как она $s";
        Kind kind = kindRepository.getById(id);
        if (kind == null) {
            rm.addFlashAttribute("error", String.format(errorMessage, "не найдена"));
            return String.format("redirect:/dictionaries/kinds/edit/%s", id);
        }

        if (!budgetRepository.getByKindId(id).isEmpty()) {
            rm.addFlashAttribute("error", String.format(errorMessage, "используется в бюджете"));
            return String.format("redirect:/dictionaries/kinds/edit/%s", id);
        }

        if (!regularOperationRepository.getByKindId(id).isEmpty()) {
            rm.addFlashAttribute("error", String.format(errorMessage, "используется в регулярных операциях"));
            return String.format("redirect:/dictionaries/kinds/edit/%s", id);
        }

        kindRepository.deleteById(id);
        return "redirect:/dictionaries/kinds";
    }

    @PostMapping("/edit")
    public String editKind(@Valid @ModelAttribute KindTo kindTo,
                                 @RequestParam(name="referer", defaultValue = "") String referer,
                                 RedirectAttributes rm) {
        if(kindTo.getId().isEmpty()) {
            kindTo.setId(null);
        }

        if(!kindRepository.getFilteredData(null, kindTo.getName(), kindTo.getType()).isEmpty()) {
            rm.addFlashAttribute("error", "Статья с наименованием '" + kindTo.getName() + "'" +
                    " уже используется в '" + kindTo.getType().getValue() + "'!");
            if (referer.isEmpty()) {
                return String.format("redirect:/dictionaries/kinds/edit/%s", kindTo.getId());
            } else {
                return referer;
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

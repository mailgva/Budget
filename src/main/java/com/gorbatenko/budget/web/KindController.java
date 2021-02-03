package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.*;
import com.gorbatenko.budget.to.KindTo;
import com.gorbatenko.budget.util.SecurityUtil;
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
        model.addAttribute("kind", kindRepository.findById(id).get());

        model.addAttribute("pageName", "Изменение");

        return "/dictionaries/kinds/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, RedirectAttributes rm) {
        String errorMessage = "Невозможно удалить статью, так как она $s";

        User user = SecurityUtil.get().getUser();
        Kind kind = kindRepository.getKindByUserGroupAndId(user.getGroup(), id);
        if (kind == null) {
            rm.addFlashAttribute("error", String.format(errorMessage, "не найдена"));
            return String.format("redirect:/dictionaries/kinds/edit/%s", id);
        }

        if (budgetRepository.countByUser_GroupAndKind(user.getGroup(), kind) > 0) {
            rm.addFlashAttribute("error", String.format(errorMessage, "используется в бюджете"));
            return String.format("redirect:/dictionaries/kinds/edit/%s", id);
        }

        if (regularOperationRepository.countByUserGroupAndKind(user.getGroup(), kind) > 0) {
            rm.addFlashAttribute("error", String.format(errorMessage, "используется в регулярных операциях"));
            return String.format("redirect:/dictionaries/kinds/edit/%s", id);
        }

        kindRepository.deleteById(id);
        return "redirect:/dictionaries/kinds";
    }

    @PostMapping("/edit")
    public String editNewDicKind(@Valid @ModelAttribute KindTo kindTo,
                                 @RequestParam(name="referer", defaultValue = "") String referer,
                                 RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();
        if(kindTo.getId().isEmpty()) {
            kindTo.setId(null);
        }
        Kind check = kindRepository.getKindByUserGroupAndTypeAndNameIgnoreCase(user.getGroup(), kindTo.getType(), kindTo.getName());
        if(check != null) {
            rm.addFlashAttribute("error", "Статья с наименованием '" + kindTo.getName() + "'" +
                    " уже используется в '" + kindTo.getType().getValue() + "'!");
            if (referer.isEmpty()) {
                return String.format("redirect:/dictionaries/kinds/edit/%s", kindTo.getId());
            } else {
                return referer;
            }
        }

        Kind kindOld = kindRepository.getKindByUserGroupAndId(user.getGroup(), kindTo.getId());
        Kind kind = createKindFromKindTo(kindTo);
        kind.setId(kindTo.getId());
        kind = kindRepository.save(kind);

        List<Budget> budgets = budgetRepository.getBudgetByKindAndUser_Group(kindOld, user.getGroup());
        for(Budget budget : budgets) {
            budget.setKind(kind);
            budgetRepository.save(budget);
        }

        List<RegularOperation> operations = regularOperationRepository.getByKindAndUserGroup(kindOld, user.getGroup());
        for(RegularOperation operation : operations) {
            operation.setKind(kind);
            regularOperationRepository.save(operation);
        }

        rm.addFlashAttribute("kindId", kind.getId());
        return (referer.isEmpty() ? "redirect:/dictionaries/kinds" : "redirect:" + referer);
    }

    private Kind createKindFromKindTo(KindTo kindTo) {
        User user = SecurityUtil.get().getUser();
        Kind kind = new Kind(kindTo.getType(), kindTo.getName(), user.getGroup());
        return kind;
    }
}

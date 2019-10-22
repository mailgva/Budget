package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.to.KindTo;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/kinds/")
public class KindController extends AbstractWebController{


    @GetMapping("/create")
    public String create() {
        return "/dictionaries/kinds/create";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, @RequestParam(name="error", defaultValue = "") String error, Model model) {
        if(!error.isEmpty()) {
            model.addAttribute("error", error);
        }
        model.addAttribute("kind", kindRepository.findById(id).get());
        return "/dictionaries/kinds/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();
        Kind kind = kindRepository.findKindByUserGroupAndId(user.getGroup(), id);
        if (budgetRepository.countByUser_GroupAndKind(user.getGroup(), kind) > 0) {
            rm.addFlashAttribute("error", "Невозможно удалить статью, так как она уже используется");
            return String.format("redirect:/dictionaries/kinds/edit/%s", id);
        }

        kindRepository.deleteById(id);
        return "redirect:/dictionaries/kinds/kinds";
    }

    @PostMapping("/create")
    public String createNewDicKind(@ModelAttribute KindTo kindTo, RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();
        Kind check = kindRepository.findKindByUserGroupAndTypeAndNameIgnoreCase(user.getGroup(), kindTo.getType(), kindTo.getName());
        if(check != null) {
            rm.addFlashAttribute("error", "Создание невозможно! Статья с наименованием '" + kindTo.getName() + "'" +
                    " уже используется в '" + kindTo.getType().getValue() + "'!");
            return "redirect:/dictionaries/kinds/create";
        }

        Kind kind = createKindFromKindTo(kindTo);
        kind.setId(kindTo.getId());
        kindRepository.save(kind);
        return "redirect:/dictionaries/kinds";
    }

    @PostMapping("/edit")
    public String editNewDicKind(@ModelAttribute KindTo kindTo, RedirectAttributes rm) {
        User user = SecurityUtil.get().getUser();
        Kind check = kindRepository.findKindByUserGroupAndTypeAndNameIgnoreCase(user.getGroup(), kindTo.getType(), kindTo.getName());
        if(check != null) {
            rm.addFlashAttribute("error", "Изменение невозможно! Статья с наименованием '" + kindTo.getName() + "'" +
                    " уже используется в '" + kindTo.getType().getValue() + "'!");
            return String.format("redirect:/dictionaries/kinds/edit/%s", kindTo.getId());
        }

        Kind kindOld = kindRepository.findKindByUserGroupAndId(user.getGroup(), kindTo.getId());
        Kind kind = createKindFromKindTo(kindTo);
        kind.setId(kindTo.getId());
        kind = kindRepository.save(kind);
        List<Budget> budgets = budgetRepository.getBudgetBykindAndUser_Group(kindOld, user.getGroup());
        for(Budget budget : budgets) {
            budget.setKind(kind);
            budgetRepository.save(budget);
        }
        return "redirect:/dictionaries/kinds";
    }

    private Kind createKindFromKindTo(KindTo kindTo) {
        User user = SecurityUtil.get().getUser();
        Kind kind = new Kind(kindTo.getType(), kindTo.getName(), user.getGroup());
        return kind;
    }
}

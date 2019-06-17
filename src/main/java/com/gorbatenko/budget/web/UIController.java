package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.to.BudgetTo;
import com.gorbatenko.budget.to.KindTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;


@Controller
@RequestMapping("/")
public class UIController {

    @Autowired
    BudgetRepository repository;

    @GetMapping("/")
    public String getMain(Model model) {
        Double remain = repository.findAll().stream()
                    .mapToDouble(budget -> (budget.getKind().getType().equals(Type.SPENDING) ? -1.0 : 1.0) * budget.getPrice())
                    .sum();
        model.addAttribute("remain", remain);
        return "main";
    }

    @GetMapping("/statistic")
    public String getStatistic(Model model) {
        model.addAttribute("listBudget", repository.findAll());
        return "statistic";
    }

    @GetMapping("/statistic/{type}")
    public String getBudgetByType(@PathVariable("type") String type,  Model model) {
        Type value;
        try {
            value = Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            value = Type.SPENDING;
        }

        model.addAttribute("listBudget",
                repository.getBudgetByKindTypeOrderByCreateDateTime(value));
        return "statistic";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/create/{type}")
    public String create(@PathVariable("type") String type, Model model) {
        model.addAttribute("type",  Type.valueOf(type.toUpperCase())); //type.toUpperCase()
        model.addAttribute("kinds",
                kindRepository.findByType(Type.valueOf(type.toUpperCase())));
        return "create";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        Budget budget = repository.findById(id).get();
        model.addAttribute("budget", budget );
        model.addAttribute("kinds",
                kindRepository.findByType(budget.getKind().getType()));
        return "edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") String id, Model model) {
        repository.deleteById(id);
        return getStatistic(model);
    }

    @GetMapping("/dictionaries")
    public String getDictionaries() {
        return "dictionaries";
    }

    @GetMapping("/dictionary/{name}")
    public String getDictionary(@PathVariable("name") String name, Model model) {
        if(name.equalsIgnoreCase("KINDS")) {
            model.addAttribute("kinds",
                    kindRepository.findAll());
        }
        return "kinds";
    }

    @GetMapping("/dictionary/kinds/create")
    public String createDicKind() {
        return "createDicKind";
    }

    @PostMapping("/dictionary/kinds/create")
    public String createNewDicKind(@ModelAttribute KindTo kindTo) {
        Kind kind = createKindFromKindTo(kindTo);
        //kind.setUser(repository.findAll().stream().findFirst().get().getUser());
        kind.setId(kindTo.getId());
        kindRepository.save(kind);
        return "redirect:/dictionary/kinds";
    }

    @PostMapping("/")
    public String createNewBudgetItem(@ModelAttribute BudgetTo budgetTo) {
        Budget budget = createBudgetFromBudgetTo(budgetTo);
        budget.setUser(repository.findAll().stream().findFirst().get().getUser());
        budget.setId(budgetTo.getId());
        repository.save(budget);
        return "redirect:/statistic";
    }

    @GetMapping("/bygroup/{group}")
    public String getBudgetByGroup(@PathVariable("group") String group,  Model model) {
        model.addAttribute("listBudget", repository.getBudgetByUserGroup(group));
        return "statistic";
    }

    @Autowired
    private KindRepository kindRepository;

    public Budget createBudgetFromBudgetTo(BudgetTo b) {
        Kind kind = kindRepository.findByNameIgnoreCase(b.getKind());
        return new Budget(null, kind, LocalDateTime.of(b.getDate(), LocalTime.MIN), b.getDescription(), b.getPrice());
    }

    private Kind createKindFromKindTo(KindTo kindTo) {
        Kind kind = new Kind(Type.valueOf(kindTo.getType()), kindTo.getName());
        return kind;
    }

}

package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.Item;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.ItemRepository;
import com.gorbatenko.budget.to.BudgetTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/")
public class UIController {

    @Autowired
    BudgetRepository repository;

    @GetMapping("/")
    public String getBudget(Model model) {
        model.addAttribute("listBudget", repository.findAll());
        return "app";
    }

    @GetMapping("/type/{type}")
    public String getBudgetByType(@PathVariable("type") String type,  Model model) {
        model.addAttribute("listBudget", repository.getBudgetByType(type.toUpperCase()));
        return "app";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/create")
    public String createPage() {
        return "create";
    }

    @PostMapping
    public String createNewBudgetItem(@ModelAttribute BudgetTo budgetTo) {
        Budget budget = createBudgetFromBudgetTo(budgetTo);
        budget.setUser(repository.findAll().stream().findFirst().get().getUser());
        repository.save(budget);
        return "redirect:/";
    }

    @GetMapping("/bygroup/{group}")
    public String getBudgetByGroup(@PathVariable("group") String group,  Model model) {
        model.addAttribute("listBudget", repository.getBudgetByUserGroup(group));
        return "app";
    }

    @Autowired
    private ItemRepository itemRepository;

    public Budget createBudgetFromBudgetTo(BudgetTo b) {
        Item item = itemRepository.findByNameIgnoreCase(b.getItem());
        return new Budget(null, b.getType(), item, b.getDate(), b.getDescription(), b.getPrice());
    }
}

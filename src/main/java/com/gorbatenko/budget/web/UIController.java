package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@Controller
public class UIController {

    @Autowired
    BudgetRepository repository;

    @GetMapping("/")
    public String getBudget(Model model) {
        model.addAttribute("listBudget", repository.findAll());
        return "app.html";
    }

    @GetMapping("/{type}")
    public String getBudgetByType(@PathVariable("type") String type,  Model model) {
        model.addAttribute("listBudget", repository.getBudgetByType(type.toUpperCase()));
        return "app.html";
    }

}

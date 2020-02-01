package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Dictionary;
import com.gorbatenko.budget.model.Kind;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/dictionaries/")
public class DictionaryController extends AbstractWebController {
    @GetMapping("/")
    public String getDictionaries() {
        return "dictionaries/dictionaries";
    }

    @GetMapping("/{name}")
    public String getDictionary(@PathVariable("name") String name, Model model) {
        Dictionary dictionary = Dictionary.valueOf(name.toUpperCase());
        switch(dictionary) {
            case KINDS:
                List<Kind> kinds = getKinds();
                Collections.sort(kinds, Comparator.comparing(o -> o.getType().getValue()));
                model.addAttribute("kinds", kinds);
                return "/dictionaries/kinds/kinds";
            case CURRENCIES:
                List<Currency> currencies = getCurrencies();
                Collections.sort(currencies, Comparator.comparing(o -> o.getName()));
                model.addAttribute("currencies", currencies);
                return "/dictionaries/currencies/currencies";
            default:
                return getDictionaries();
        }

    }
}

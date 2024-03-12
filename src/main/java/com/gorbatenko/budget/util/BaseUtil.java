package com.gorbatenko.budget.util;

import com.gorbatenko.budget.model.BudgetItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class BaseUtil {
    public static String dateToStr(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    public static String dateToStrCustom(LocalDate date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return date.format(formatter);
    }

    public static TreeMap<LocalDate, List<BudgetItem>> listBudgetToTreeMap(List<BudgetItem> listBudgetItems) {
        TreeMap<LocalDate, List<BudgetItem>> map = new TreeMap<>(Collections.reverseOrder());
        for (BudgetItem budgetItem : listBudgetItems) {
            LocalDate key = budgetItem.getDateAt();
            if (map.containsKey(key)) {
                map.get(key).add(budgetItem);
            } else {
                map.put(key, new ArrayList<>(Arrays.asList(budgetItem)));
            }
        }
        return map;
    }

}

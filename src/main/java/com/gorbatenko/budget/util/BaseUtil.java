package com.gorbatenko.budget.util;

import com.gorbatenko.budget.model.Budget;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BaseUtil {
    public static String dateToStr(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    public static LocalDateTime setTimeZoneOffset(LocalDate ld) {
        TimeZone timeZone = TimeZone.getDefault();
        long hours = TimeUnit.MILLISECONDS.toHours(timeZone.getOffset(System.currentTimeMillis()));
        LocalDateTime ldt = LocalDateTime.of(ld, LocalTime.MIN).plusHours(hours);
        return ldt;
    }

    public static TreeMap<String, List<Budget>> listBudgetToTreeMap(List<Budget> listBudget) {
        TreeMap<String, List<Budget>> map = new TreeMap<>(Collections.reverseOrder());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        listBudget.stream()
                .forEach(budget -> {
                    String key = budget.getDate().format(formatter);
                    if(map.containsKey(key)) {
                        map.get(key).add(budget);
                    } else {
                        map.put(key, new ArrayList<>(Arrays.asList(budget)));
                    }
                });
        return map;
    }
}

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

    public static String dateToStrCustom(LocalDate date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return date.format(formatter);
    }


    public static LocalDateTime setTimeZoneOffset(LocalDate ld) {
        TimeZone timeZone = TimeZone.getDefault();
        long hours = TimeUnit.MILLISECONDS.toHours(timeZone.getOffset(System.currentTimeMillis()));
        return LocalDateTime.of(ld, LocalTime.MIN).plusHours(hours);
    }

    public static TreeMap<LocalDate, List<Budget>> listBudgetToTreeMap(List<Budget> listBudget) {
        TreeMap<LocalDate, List<Budget>> map = new TreeMap<>(Collections.reverseOrder());
        for (Budget budget : listBudget) {
            LocalDate key = budget.getDate().toLocalDate();
            if (map.containsKey(key)) {
                map.get(key).add(budget);
            } else {
                map.put(key, new ArrayList<>(Arrays.asList(budget)));
            }
        }
        return map;
    }

    public static String getStrDateTime(LocalDateTime ldt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return ldt.format(formatter);
    }

    public static String getStrDate(LocalDateTime ldt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return ldt.format(formatter);
    }

    public static String getStrYearMonthDay(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(formatter);
    }

    public static String getStrYearMonth(LocalDateTime ldt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        return ldt.format(formatter);
    }
}

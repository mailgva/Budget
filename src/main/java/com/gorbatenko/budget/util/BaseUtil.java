package com.gorbatenko.budget.util;

import com.gorbatenko.budget.model.BudgetItem;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.gorbatenko.budget.web.BudgetItemController.getSumTimeZoneOffsetMinutes;


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

    public static TreeMap<LocalDate, List<BudgetItem>> listBudgetToTreeMap(List<BudgetItem> listBudgetItems, HttpServletRequest request) {
        int sumTimeZoneOffsetMinutes = getSumTimeZoneOffsetMinutes(request);
        TreeMap<LocalDate, List<BudgetItem>> map = new TreeMap<>(Collections.reverseOrder());
        for (BudgetItem budgetItem : listBudgetItems) {
            budgetItem.setCreateDateTime(budgetItem.getCreateDateTime().plusMinutes(sumTimeZoneOffsetMinutes));
            LocalDate key = budgetItem.getDate().toLocalDate();
            if (map.containsKey(key)) {
                map.get(key).add(budgetItem);
            } else {
                map.put(key, new ArrayList<>(Arrays.asList(budgetItem)));
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

    public static String getStrYear(LocalDateTime ldt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return ldt.format(formatter);
    }
}

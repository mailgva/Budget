package com.gorbatenko.budget.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public enum GroupPeriod {
    BY_DEFAULT("", "yyyy-MM-dd"),
    BY_DAYS("по дням", "yyyy-MM-dd"),
    BY_MONTHS("по месяцам","yyyy-MM"),
    BY_YEARS("по годам","yyyy");

    private final String value;
    private String fmt;

    GroupPeriod(String value, String fmt) {
        this.value = value;
        this.fmt = fmt;
    }

    public String getValue() {
        return value;
    }

    public String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(this.fmt));
    }
}

package com.gorbatenko.budget.util;

public enum GroupPeriod {
    BY_DEFAULT(""), BY_DAYS("по дням"), BY_MONTHS("по месяцам"), BY_YEARS("по годам");

    private final String value;

    GroupPeriod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

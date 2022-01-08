package com.gorbatenko.budget.model;

public enum Dictionary {
    KINDS("Виды Приходов/Расходов"), CURRENCIES("Валюты");

    private final String value;

    Dictionary(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


}

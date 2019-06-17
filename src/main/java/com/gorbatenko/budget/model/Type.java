package com.gorbatenko.budget.model;

public enum Type {
    PROFIT("Приход"), SPENDING("Расход");

    private String value;


    Type(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Type{" +
                "value='" + value + '\'' +
                '}';
    }
}

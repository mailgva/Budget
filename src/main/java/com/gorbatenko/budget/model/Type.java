package com.gorbatenko.budget.model;

public enum Type {
    PROFIT("Приход"), SPENDING("Расход");

    private final String value;


    Type(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Type from(String name) {
        for (Type type : Type.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

}

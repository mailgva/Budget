package com.gorbatenko.budget.model;

public enum Every {
    DEFINITE_DAY_OF_MONTH(0, "В указанный день (число) месяца"),
    DAY(1, "Каждый день"),
    MONDAY(2,"По понедельникам"),
    TUESDAY(3,"По вторникам"),
    WEDNESDAY(4, "По средам"),
    THURSDAY(5, "Пр четвергам"),
    FRIDAY(6, "По пятницам"),
    SATURDAY(7,"По субботам"),
    SUNDAY(8, "По воскресеньям");

    private final int posit;

    private final String value;

    Every(int posit, String value) {
        this.posit = posit;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public int getPosit() {
        return posit;
    }
}

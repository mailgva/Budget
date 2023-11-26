package com.gorbatenko.budget.web.charts;

public enum ChartType {
    HORIZONTALBAR("horizontalBar"), DOUGHNUT("doughnut"), BAR("bar"), LINE("line");

    private final String value;

    public String getValue() {
        return value;
    }

    ChartType(String value) {
        this.value = value;
    }
}

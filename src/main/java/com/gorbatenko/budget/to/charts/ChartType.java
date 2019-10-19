package com.gorbatenko.budget.to.charts;

public enum ChartType {
    HORIZONTALBAR("horizontalBar"), DOUGHNUT("doughnut");

    private String value;

    public String getValue() {
        return value;
    }

    ChartType(String value) {
        this.value = value;
    }
}

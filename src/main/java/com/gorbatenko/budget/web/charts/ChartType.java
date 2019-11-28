package com.gorbatenko.budget.web.charts;

public enum ChartType {
    HORIZONTALBAR("horizontalBar"), DOUGHNUT("doughnut"), BARCHART("bar");

    private String value;

    public String getValue() {
        return value;
    }

    ChartType(String value) {
        this.value = value;
    }
}

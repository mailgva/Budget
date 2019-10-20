package com.gorbatenko.budget.to.charts;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartDatasetsDoughnut extends ChartDatasets{
    private String[] hoverBackgroundColor;


    public ChartDatasetsDoughnut(String label, String[] data, String[] backgroundColor, String[] hoverBackgroundColor) {
        this.label = label;
        this.data = data;
        this.backgroundColor = backgroundColor;
        this.hoverBackgroundColor = hoverBackgroundColor;
    }
}

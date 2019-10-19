package com.gorbatenko.budget.to.charts;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartData {
    private String[] labels;
    private ChartDatasets[] datasets;
}

package com.gorbatenko.budget.web.charts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartData {
    private String[] labels;
    private ChartDatasets[] datasets;
}

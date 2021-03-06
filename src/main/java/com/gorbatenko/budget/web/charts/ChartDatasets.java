package com.gorbatenko.budget.web.charts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
abstract public class ChartDatasets  {
    String[] data;
    String[] backgroundColor;
    String label;
}

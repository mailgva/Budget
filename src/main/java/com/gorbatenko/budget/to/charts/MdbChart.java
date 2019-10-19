package com.gorbatenko.budget.to.charts;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class MdbChart {
    private String type;
    private ChartData data;
    private Map<String, Object> options;
}

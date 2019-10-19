package com.gorbatenko.budget.to.charts;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartDatasetsHorizont extends ChartDatasets{
    final static int BORDER_WIDTH = 1;

    private boolean fill;
    private String[] borderColor;
    private int borderWidth = BORDER_WIDTH;

    public ChartDatasetsHorizont(String label, String[] data, boolean fill, String[] backgroundColor, String[] borderColor) {
        this.label = label;
        this.data = data;
        this.fill = fill;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
    }


}

package com.gorbatenko.budget.to;

import lombok.Data;

@Data
public class RemainderTo {
    private Double profit;
    private Double spending;
    private Double remainder;

    public RemainderTo(Double profit, Double spending) {
        this.profit = profit;
        this.spending = spending;
        this.remainder = profit - spending;
    }
}

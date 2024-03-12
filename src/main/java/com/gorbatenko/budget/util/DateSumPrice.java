package com.gorbatenko.budget.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class DateSumPrice implements IDateSumPrice {
    private LocalDate date;
    private Double sumPrice;
}

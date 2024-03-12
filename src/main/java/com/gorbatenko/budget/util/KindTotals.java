package com.gorbatenko.budget.util;

import com.gorbatenko.budget.model.Kind;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KindTotals {
    private Kind kind;
    private Double sumPrice;
    private Long count;
    private LocalDate minCreateDate;
    private LocalDate maxCreateDate;
}
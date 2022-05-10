package com.gorbatenko.budget.util;

import com.gorbatenko.budget.model.Kind;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KindTotals {
    private Kind kind;
    private Double sumPrice;
    private Long count;
    private LocalDateTime minCreateDateTime;
    private LocalDateTime maxCreateDateTime;
}
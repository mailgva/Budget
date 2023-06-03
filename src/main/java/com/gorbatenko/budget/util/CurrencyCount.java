package com.gorbatenko.budget.util;

import com.gorbatenko.budget.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyCount {
    private Currency currency;
    private Long count;
}
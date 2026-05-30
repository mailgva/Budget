package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gorbatenko.budget.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BudgetItemTo extends BaseEntity {
    private UUID kindId;
    private UUID currencyId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateAt;

    private String description;

    @NotNull
    private Double price;

    public String getStrDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateAt.format(formatter);
    }
}

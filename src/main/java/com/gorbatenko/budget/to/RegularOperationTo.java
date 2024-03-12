package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.model.Every;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegularOperationTo extends BaseEntity {
    private Every every;

    private Integer dayOfMonth = 1;

    private UUID kindId;

    private String description;

    @NotNull
    private Double price;

    private UUID currencyId;

}

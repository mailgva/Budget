package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.model.Every;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegularOperationTo extends BaseEntity {
    private Every every;

    private Integer dayOfMonth = 1;

    private String kindId;

    private String description;

    @NotNull
    private Double price;

    private String currencyId;

}

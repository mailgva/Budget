package com.gorbatenko.budget.model;

import com.gorbatenko.budget.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "regular_operations")
public class RegularOperation extends BaseEntity {

    @NotNull
    private User user;

    @NotNull
    private String userGroup;

    @NotNull
    private int countUserTimezomeOffsetMinutes;

    @NotNull
    private Every every;

    @NotNull
    private Integer dayOfMonth = 1;

    @NotNull
    private Kind kind;

    private String description;

    @NotNull
    private Double price;

    @NotNull
    private Currency currency;

}

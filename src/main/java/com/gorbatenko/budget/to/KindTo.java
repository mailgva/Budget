package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.model.Type;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KindTo extends BaseEntity {
    @NotNull
    private String name;

    @NotNull
    private Type type;

    private boolean hidden = false;

    public KindTo(Type type, String name, boolean hidden) {
        this.type = type;
        this.name = name;
        this.hidden = hidden;
    }
}

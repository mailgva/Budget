package com.gorbatenko.budget.to;

import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.model.Type;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KindTo extends BaseEntity {
    private String name;

    private Type type;

    public KindTo(Type type, String name) {
        this.type = type;
        this.name = name;
    }
}
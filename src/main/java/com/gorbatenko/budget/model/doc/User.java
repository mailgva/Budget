package com.gorbatenko.budget.model.doc;

import com.gorbatenko.budget.BaseEntity;
import lombok.Data;

@Data
public class User extends BaseEntity {
    public User(String id, String name) {
        super(id);
        this.name = name;
    }

    private String name;

}

package com.gorbatenko.budget.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "group")
public class UserGroup extends BaseEntity {
    @Indexed
    private String name;

    public UserGroup(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserGroup{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                '}';
    }
}

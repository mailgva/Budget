package com.gorbatenko.budget.model;

import com.gorbatenko.budget.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "kind")
public class Kind extends BaseEntity {
    @Indexed
    private String name;

    private Type type;

    private String userGroup;

    public Kind(Type type, String name, String userGroup) {
        this.type = type;
        this.name = name;
        this.userGroup = userGroup;
    }

    @Override
    public String toString() {
        return "Kind{" +
                "id=" + getId() +
                ", name='" + name +
                ", type=" + type +
                ", userGroup=" + userGroup +
                '}';
    }
}

package com.gorbatenko.budget.model;

import com.gorbatenko.budget.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "kinds")
public class Kind extends BaseEntity implements Comparable<Kind> {
    public static final String EXCHANGE_NAME = "Обмен валюты";

    private String name;

    @Enumerated(EnumType.STRING)
    private Type type;

    private UUID userGroup;

    private Boolean hidden = false;

    public Kind(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Kind(Type type, String name, boolean hidden) {
        this.type = type;
        this.name = name;
        this.hidden = hidden;
    }

    public Kind(UUID id, Type type, String name, boolean hidden) {
        super(id);
        this.type = type;
        this.name = name;
        this.hidden = hidden;
    }

    public Kind(Type type, String name, UUID userGroup) {
        this.type = type;
        this.name = name;
        this.userGroup = userGroup;
    }

    public Kind(UUID id, String name, Type type) {
        super(id);
        this.name = name;
        this.type = type;
    }

    @Override
    public int compareTo(Kind o) {
        int typeCompare = this.getType().compareTo(o.getType());
        if (typeCompare != 0) {
            return typeCompare;
        }
        int nameCompare = this.getName().compareTo(o.getName());
        if (nameCompare != 0) {
            return nameCompare;
        }
        return this.getId().compareTo(o.getId());
    }

}

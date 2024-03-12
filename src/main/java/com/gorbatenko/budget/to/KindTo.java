package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.config.Deserializer;
import com.gorbatenko.budget.model.Type;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KindTo extends BaseEntity {
    @NotNull
    private String name;

    @NotNull
    private Type type;

    @JsonDeserialize(using = Deserializer.OnOffDeserializer.class)
    private Boolean hidden = false;

    public KindTo(UUID id, Type type, String name, Boolean hidden) {
        super(id);
        this.type = type;
        this.name = name;
        this.hidden = hidden;
    }

    public KindTo(Type type, String name, Boolean hidden) {
        this.type = type;
        this.name = name;
        this.hidden = hidden;
    }
}

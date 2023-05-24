package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.model.Type;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KindTo extends BaseEntity {
    @NotNull
    private String name;

    @NotNull
    private Type type;

    @JsonDeserialize(using = OnOffDeserializer.class)
    private boolean hidden = false;

    public KindTo(Type type, String name, boolean hidden) {
        this.type = type;
        this.name = name;
        this.hidden = hidden;
    }

    public static class OnOffDeserializer extends JsonDeserializer<Boolean> {
        @Override
        public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return "on".equalsIgnoreCase(parser.getText());
        }
    }
}

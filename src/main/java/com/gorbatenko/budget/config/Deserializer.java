package com.gorbatenko.budget.config;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class Deserializer {
    public static class OnOffDeserializer extends ValueDeserializer<Boolean> {
        @Override
        public Boolean deserialize(JsonParser parser, DeserializationContext context) {
            return "on".equalsIgnoreCase(parser.getString());
        }
    }
}

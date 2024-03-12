package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.config.Deserializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyTo extends BaseEntity {
  @NotNull
  private String name;

  @JsonDeserialize(using = Deserializer.OnOffDeserializer.class)
  private Boolean hidden = false;

  public CurrencyTo(String name, Boolean hidden) {
    this.name = name;
    this.hidden = hidden;
  }
}

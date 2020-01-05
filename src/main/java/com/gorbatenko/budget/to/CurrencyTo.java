package com.gorbatenko.budget.to;

import com.gorbatenko.budget.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class CurrencyTo  extends BaseEntity {
  @NotNull
  private String name;

  public CurrencyTo(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "CurrencyTo{" +
            "id='" + getId() + '\'' +
            ", name='" + name + '\'' +
            '}';
  }
}

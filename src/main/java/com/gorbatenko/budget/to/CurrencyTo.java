package com.gorbatenko.budget.to;

import com.gorbatenko.budget.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class CurrencyTo extends BaseEntity {
  @NotNull
  private String name;

  private boolean hidden;

  public CurrencyTo(String name, boolean hidden) {
    this.name = name;
    this.hidden = hidden;
  }

  @Override
  public String toString() {
    return "CurrencyTo{" +
            "id='" + getId() + '\'' +
            ", name='" + name + '\'' +
            ", hidden='" + hidden + '\'' +
            '}';
  }
}

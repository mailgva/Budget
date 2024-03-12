package com.gorbatenko.budget.model;

import com.gorbatenko.budget.BaseEntity;
import jakarta.persistence.Entity;
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
@Table(name = "currencies")
public class Currency extends BaseEntity implements Comparable<Currency> {
  private String name;
  private UUID userGroup;
  private Boolean hidden = false;

  public Currency(String name) {
    this.name = name;
  }

  public Currency(String name, UUID userGroup) {
    this.name = name;
    this.userGroup = userGroup;
  }

  public Currency(String name, boolean hidden) {
    this.name = name;
    this.hidden = hidden;
  }

  @Override
  public int compareTo(Currency o) {
    return this.getName().compareTo(o.getName());
  }

  @Override
  public String toString() {
    return "Currency{" +
            "id='" + getId() + '\'' +
            ", name='" + name + '\'' +
            ", userGroup='" + userGroup + '\'' +
            ", hidden='" + hidden + '\'' +
            '}';
  }
}

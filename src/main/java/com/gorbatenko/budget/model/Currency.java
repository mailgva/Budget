package com.gorbatenko.budget.model;

import com.gorbatenko.budget.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "currencies")
public class Currency extends BaseEntity implements Comparable {
  @Indexed
  private String name;

  private String userGroup;

  private boolean hidden = false;

  public Currency(String name) {
    this.name = name;
  }

  public Currency(String name, String userGroup) {
    this.name = name;
    this.userGroup = userGroup;
  }

  public Currency(String name, boolean hidden) {
    this.name = name;
    this.hidden = hidden;
  }

  @Override
  public int compareTo(Object o) {
    Currency other = (Currency) o;
    return this.getName().compareTo(other.getName());
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

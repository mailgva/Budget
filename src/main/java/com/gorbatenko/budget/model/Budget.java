package com.gorbatenko.budget.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Document(collection = "budget")
public class Budget extends BaseEntity {

    private User user;

    private Type type;

    private Item item;

    private LocalDate localDate;

    private String description;

    private Double price;

    public Budget(User user, Type type, Item item, LocalDate localDate, String description, Double price) {
        this.user = user;
        this.type = type;
        this.item = item;
        this.localDate = localDate;
        this.description = description;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + getId() +
                ", user=" + user +
                ", type=" + type +
                ", item=" + item +
                ", localDate=" + localDate +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}

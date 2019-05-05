package com.gorbatenko.budget.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "budget")
public class Budget extends BaseEntity {

    private User user;

    private Type type;

    private Item item;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private LocalDateTime createDateTime;

    private String description;

    private Double price;

    public Budget(User user, Type type, Item item, LocalDate date, String description, Double price) {
        this.user = user;
        this.type = type;
        this.item = item;
        this.date = date;
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
                ", date=" + date +
                ", createDateTime=" + createDateTime +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}

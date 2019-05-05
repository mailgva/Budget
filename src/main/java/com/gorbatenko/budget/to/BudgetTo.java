package com.gorbatenko.budget.to;

import com.gorbatenko.budget.model.BaseEntity;
import com.gorbatenko.budget.model.Item;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "budget")
public class BudgetTo {

    private Type type;

    private String item;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String description;

    private Double price;

    public BudgetTo(Type type, String item, LocalDate date, String description, Double price) {
        this.type = type;
        this.item = item;
        this.date = date;
        this.description = description;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Budget{" +
                ", type=" + type +
                ", item=" + item +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}

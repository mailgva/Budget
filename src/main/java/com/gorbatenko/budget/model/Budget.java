package com.gorbatenko.budget.model;

import com.gorbatenko.budget.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@Document(collection = "budget")
public class Budget extends BaseEntity {

    private User user;

    private String userGroup;

    private Kind kind;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime date;


    private LocalDateTime createDateTime;

    private String description;

    @NotNull
    private Double price;

    public Budget(User user, Kind kind, LocalDateTime date, String description, Double price) {
        this.user = user;
        this.kind = kind;
        this.date = date;
        this.createDateTime = LocalDateTime.now();
        this.description = description;
        this.price = price;
    }

    public Budget(User user, String userGroup, Kind kind, LocalDateTime date, String description, Double price) {
        this.user = user;
        this.userGroup = userGroup;
        this.kind = kind;
        this.date = date;
        this.createDateTime = LocalDateTime.now();
        this.description = description;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + getId() +
                ", user=" + user +
                ", userGroup=" + userGroup +
                ", kind=" + kind +
                ", date=" + date +
                ", createDateTime=" + createDateTime +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }

    public String getStrDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return date.format(formatter);
    }

    public String getStrDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

}

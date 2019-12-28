package com.gorbatenko.budget.to;

import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.model.Type;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class BudgetTo extends BaseEntity {

    //@NotNull
    private String kind;

    private String kindId;

    private String currencyId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String description;

    @NotNull
    private Double price;

    public BudgetTo(String kind, LocalDate date, String description, Double price) {
        this.kind = kind;
        this.date = date;
        this.description = description;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "kind=" + kind +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }


    public String getStrDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }
}

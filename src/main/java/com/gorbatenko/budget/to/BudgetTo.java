package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.gorbatenko.budget.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BudgetTo extends BaseEntity {

    //@NotNull
    private String kind;

    private String kindId;

    private String currencyId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
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
        return "BudgetItem{" +
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

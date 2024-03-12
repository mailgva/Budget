package com.gorbatenko.budget.model;

import com.gorbatenko.budget.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "budget_items")
@SuperBuilder
public class BudgetItem extends BaseEntity {

    @NotNull
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private UUID userGroup;

    @NotNull
    @OneToOne
    @JoinColumn(name = "kind_id", referencedColumnName = "id")
    private Kind kind;

    @NotNull
    @OneToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "date_at")
    private LocalDate dateAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "price")
    private Double price;

    public BudgetItem(User user, Kind kind, LocalDate dateAt, String description, Double price, Currency currency) {
        this.user = user;
        this.kind = kind;
        this.dateAt = dateAt;
        this.createdAt = LocalDateTime.now();
        this.description = description;
        this.price = price;
        this.currency = currency;
    }

    public BudgetItem(User user, UUID userGroup, Kind kind, LocalDate dateAt, String description, Double price, Currency currency) {
        this.user = user;
        this.userGroup = userGroup;
        this.kind = kind;
        this.dateAt = dateAt;
        this.createdAt = LocalDateTime.now();
        this.description = description;
        this.price = price;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "BudgetItem{" +
                "id=" + getId() +
                ", user=" + user +
                ", userGroup=" + userGroup +
                ", kind=" + kind +
                ", dateAt=" + dateAt +
                ", createdAt=" + createdAt +
                ", description='" + description + '\'' +
                ", price=" + price + '\'' +
                ", currency=" + currency.getName() +
                '}';
    }

    public String getStrDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateAt.format(formatter);
    }

    public String getStrYearMonth() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        return dateAt.format(formatter);
    }

    public String getStrYear() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return dateAt.format(formatter);
    }
}

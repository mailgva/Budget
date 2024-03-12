package com.gorbatenko.budget.model;

import com.gorbatenko.budget.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "regular_operations")
public class RegularOperation extends BaseEntity {

    @NotNull
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @NotNull
    @Column(name = "user_group")
    private UUID userGroup;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Every every;

    @NotNull
    @Column(name = "day_of_month")
    private Integer dayOfMonth = 1;

    @NotNull
    @OneToOne
    @JoinColumn(name = "kind_id", referencedColumnName = "id")
    private Kind kind;

    private String description;

    @NotNull
    private Double price;

    @NotNull
    @OneToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

}

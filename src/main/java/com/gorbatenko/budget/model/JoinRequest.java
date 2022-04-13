package com.gorbatenko.budget.model;

import com.gorbatenko.budget.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "join_requests")
public class JoinRequest extends BaseEntity {

    @NotNull
    private User user;

    @NotNull
    private String userGroup;

    @NotNull
    private LocalDateTime created;

    private LocalDateTime accepted;

    private LocalDateTime declined;
}

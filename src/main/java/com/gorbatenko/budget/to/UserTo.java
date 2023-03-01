package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Type;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTo extends BaseEntity {
    @NotBlank
    private String name;

    @Email
    private String email;

    @NotBlank
    @Size(min = 5, max = 100)
    private String password;
}

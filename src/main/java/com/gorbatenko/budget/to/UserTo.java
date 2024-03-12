package com.gorbatenko.budget.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gorbatenko.budget.BaseEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

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

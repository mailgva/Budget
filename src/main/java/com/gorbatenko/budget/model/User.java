package com.gorbatenko.budget.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@Document(collection = "user")
public class User extends BaseEntity {

    private UserGroup userGroup;

    private String name;

    @Email
    @Indexed /*(unique=true)*/
    private String email;


    public User(UserGroup userGroup, String name, @Email String email) {
        this.userGroup = userGroup;
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                ", userGroup='" + userGroup + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

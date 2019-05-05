package com.gorbatenko.budget.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@Document(collection = "user")
public class User extends BaseEntity {

    private String name;

    // By default contains self id. If user is a member group, then field will be contains id group owner.
    private String group;

    @Email
    @Indexed /*(unique=true)*/
    private String email;


    public User(String name, @Email String email) {
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                ", group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

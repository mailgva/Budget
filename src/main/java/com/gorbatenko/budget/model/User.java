package com.gorbatenko.budget.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.gorbatenko.budget.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User extends BaseEntity {

    // By default contains self id. If user is a member group, then field will be contains id group owner.
    private String group;

    private String name;

    private Currency currencyDefault;

    @Email
    @Indexed /*(unique=true)*/
    private String email;

    @NotBlank
    @Size(min = 5, max = 100)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Set<Role> roles = new HashSet<>();

    public User(String name, @Email String email) {
        this.name = name;
        this.email = email;
    }

    public User(String name, String email, String password, Collection<Role> roles) {
        this.name = name;
        this.email = email;
        this.password = password;
        setRoles(roles);
    }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        roles.add(role);
    }


    public User(String id, String name, String email, String password) {
        this.setId(id);
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        User that = (User) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() == null ? 0 : getId().hashCode();
    }


    @Override
    public String toString() {
        return "User{" +
            "id=" + getId() +
            ", name='" + name + '\'' +
            ", email='" + email + '\'' +
            ", password='" + password + '\'' +
            ", group='" + group + '\'' +
            ", currencyDefault='" + currencyDefault.getName() + '\'' +
            '}';
    }


    public void setRoles(Collection<Role> roles) {
        this.roles = CollectionUtils.isEmpty(roles) ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
    }
}

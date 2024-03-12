package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IUserRepository extends JpaRepository<User, UUID> {

    default User saveUser(User s) throws Exception {
        s.setEmail(s.getEmail().toLowerCase());
        if(getByEmail(s.getEmail()) != null) {
            throw new Exception("Пользователь с таким email (" + s.getEmail() + ") уже существует!");
        }

        User user = save(s);
        if(user.getUserGroup() == null) {
            user.setUserGroup(user.getId());
            user = save(user);
        }
        return save(user);
    }

    User getByEmail(String email);

    List<User> findByUserGroup(UUID userGroup);
}

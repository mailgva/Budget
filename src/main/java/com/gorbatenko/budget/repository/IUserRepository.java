package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.User;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public interface IUserRepository extends MongoRepository<User, String> {

    default User saveUser(User s) throws Exception {
        if(getByEmail(s.getEmail()) != null) {
            throw new Exception("Пользователь с таким email (" + s.getEmail() + ") уже существует!");
        }

        User user = save(s);
        if(user.getGroup() == null) {
            user.setGroup(user.getId());
            user = save(user);
        }
        return save(user);
    }

    User getByEmail(String email);

    List<User> getByGroupIgnoreCase(String name);
}

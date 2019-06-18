package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    default User saveUser(User s) {
        User user = save(s);
        if(user.getGroup() == null) {
            user.setGroup(user.getId());
            user = save(user);
        }
        return user;
    };

    User findByNameIgnoreCase(String name);
}

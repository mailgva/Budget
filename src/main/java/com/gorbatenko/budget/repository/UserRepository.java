package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.User;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    default User saveUser(User s) {
        User user = save(s);
        if(user.getGroup() == null) {
            user.setGroup(user.getId());
            user = save(user);
        }
        return save(user);
    };

    User findByNameIgnoreCase(String name);

    User getByEmail(String email);

    List<User> getByGroupIgnoreCase(String name);
}

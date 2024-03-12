package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final IUserRepository repository;

    public User getByEmail(String email) {
        return repository.getByEmail(email.toLowerCase());
    }


    public User saveUser(User user) throws Exception {
        return repository.saveUser(user);
    }

    public User save(User user) {
        return repository.save(user);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public List<User> findByUserGroup(UUID userGroup) {
        return repository.findByUserGroup(userGroup);
    }

    public User findById(UUID id) {
        return repository.findById(id).orElse(null);
    }
}

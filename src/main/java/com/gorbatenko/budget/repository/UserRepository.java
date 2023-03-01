package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private IUserRepository repository;

    @Autowired
    public void setRepository(IUserRepository repository) {
        this.repository = repository;
    }

    public User getByEmail(String email) {
        return repository.getByEmail(email);
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

    public List<User> getByGroupIgnoreCase(String name) {
        return repository.getByGroupIgnoreCase(name);
    }

    public User findById(String id) {
        return repository.findById(id).orElse(null);
    }
}

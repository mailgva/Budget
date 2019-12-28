package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User create(User user) throws Exception;

    User save(User user);

    List<User> findAll();

    User findById(String id);

    List<User> getByGroup(String name);
}
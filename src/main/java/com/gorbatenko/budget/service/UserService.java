package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface UserService extends UserDetailsService {
    User create(User user) throws Exception;

    User save(User user);

    List<User> findAll();

    User findById(UUID id);

    List<User> findByUserGroup(UUID userGroup);

    User findByEmail(String email);

    User changeDefaultCurrency(UUID currencyId);
}
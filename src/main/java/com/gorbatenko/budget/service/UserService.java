package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User create(User user);
}
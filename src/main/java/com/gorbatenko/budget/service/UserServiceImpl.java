package com.gorbatenko.budget.service;

import static com.gorbatenko.budget.util.UserUtil.prepareToSave;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service("userService")
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthorizedUser loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.getByEmail(email.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException("User " + email + " is not found");
        }
        return new AuthorizedUser(user);
    }


    public User create(User user) throws Exception {
        Assert.notNull(user, "user must not be null");
        return repository.saveUser(prepareToSave(user, passwordEncoder));
    }

    public User save(User user) {
        return repository.save(user);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public List<User> getByGroup(String name) {
        return repository.getByGroupIgnoreCase(name);
    }
}




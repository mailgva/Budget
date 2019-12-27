package com.gorbatenko.budget.service;

import static com.gorbatenko.budget.util.UserUtil.prepareToSave;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service("userService")
public class UserServiceImpl implements UserService {
    private Map<String, Type> mapStartKinds = new HashMap();

    {
        mapStartKinds.put("Зарплата", Type.PROFIT);
        mapStartKinds.put("Доп. доход", Type.PROFIT);
        mapStartKinds.put("Продукты", Type.SPENDING);
        mapStartKinds.put("Коммунальные расходы", Type.SPENDING);
        mapStartKinds.put("Прочее", Type.SPENDING);
    }
    private final UserRepository repository;

    private final KindRepository kindRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, KindRepository kindRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.kindRepository = kindRepository;
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
        User newUser = repository.saveUser(prepareToSave(user, passwordEncoder));
        createStartKindsForUser(newUser);
        return newUser;
    }

    public User save(User user) {
        return repository.save(user);
    }

    private void createStartKindsForUser(User user) {
        String userGroupId = user.getGroup();
        for(HashMap.Entry<String, Type> entry : mapStartKinds.entrySet()) {
            kindRepository.save(new Kind(entry.getValue(), entry.getKey(), userGroupId));
        }

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




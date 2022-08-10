package com.gorbatenko.budget.service;

import static com.gorbatenko.budget.util.UserUtil.prepareToSave;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.repository.*;

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

    private final Map<String, Type> mapStartKinds = new HashMap<>();
    private final List<String> listStartCurrencies = new ArrayList<>();

    {
        mapStartKinds.put("Зарплата", Type.PROFIT);
        mapStartKinds.put("Доп. доход", Type.PROFIT);
        mapStartKinds.put("Продукты", Type.SPENDING);
        mapStartKinds.put("Коммунальные расходы", Type.SPENDING);
        mapStartKinds.put("Прочее", Type.SPENDING);

        listStartCurrencies.add("грн");
        listStartCurrencies.add("usd");
        listStartCurrencies.add("eur");
    }

    private final UserRepository repository;

    private final KindRepository kindRepository;

    private final CurrencyRepository currencyRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder,
                           KindRepository kindRepository, CurrencyRepository currencyRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.kindRepository = kindRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public AuthorizedUser loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.getByEmail(email.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException("User " + email + " is not found");
        }
        return new AuthorizedUser(user);
    }

    @Transactional
    public User create(User user) throws Exception {
        Assert.notNull(user, "User must not be null");
        User newUser = repository.saveUser(prepareToSave(user, passwordEncoder));
        createStartKindsForUser(newUser);
        createStartCurrenciesForUser(newUser);
        newUser.setCurrencyDefault(currencyRepository.getByUserGroupAndName(newUser.getGroup(),"грн"));
        return save(newUser);
    }

    public User save(User user) {
        return repository.save(user);
    }

    private void createStartKindsForUser(User user) {
        String userGroupId = user.getGroup();
        for (HashMap.Entry<String, Type> entry : mapStartKinds.entrySet()) {
            kindRepository.save(new Kind(entry.getValue(), entry.getKey(), userGroupId));
        }
    }

    private void createStartCurrenciesForUser(User user) {
        String userGroupId = user.getGroup();
        for (String currencyName : listStartCurrencies) {
            currencyRepository.save(new Currency(currencyName, userGroupId));
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

    @Override
    public User findByEmail(String email) {
        return repository.getByEmail(email);
    }

    @Override
    public User findById(String id) {
        return repository.findById(id).orElse(null);
    }
}




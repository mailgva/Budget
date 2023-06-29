package com.gorbatenko.budget.service;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.Currency;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.repository.CurrencyRepository;
import com.gorbatenko.budget.repository.KindRepository;
import com.gorbatenko.budget.repository.UserRepository;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gorbatenko.budget.model.Kind.EXCHANGE_NAME;
import static com.gorbatenko.budget.util.UserUtil.prepareToSave;

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

        mapStartKinds.put(EXCHANGE_NAME, Type.PROFIT);
        mapStartKinds.put(EXCHANGE_NAME, Type.SPENDING);

        listStartCurrencies.add("грн");
        listStartCurrencies.add("usd");
        listStartCurrencies.add("eur");
    }

    private UserRepository repository;

    private KindRepository kindRepository;

    private CurrencyRepository currencyRepository;

    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setKindRepository(KindRepository kindRepository) {
        this.kindRepository = kindRepository;
    }

    @Autowired
    public void setCurrencyRepository(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
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
        return repository.findById(id);
    }

    @Override
    public User changeDefaultCurrency(String currencyId) {
        Currency currency = currencyRepository.getById(currencyId);
        if (currency == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User user = SecurityUtil.get().getUser();
        user.setCurrencyDefault(currency);
        return this.save(user);
    }
}




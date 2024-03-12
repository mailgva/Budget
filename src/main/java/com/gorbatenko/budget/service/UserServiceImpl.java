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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static com.gorbatenko.budget.model.Kind.EXCHANGE_NAME;
import static com.gorbatenko.budget.util.UserUtil.prepareToSave;

@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    private final KindRepository kindRepository;

    private final CurrencyRepository currencyRepository;

    private final PasswordEncoder passwordEncoder;
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
        List<Currency> currenciesForUser = createStartCurrenciesForUser(newUser);
        newUser.setCurrencyDefault(currenciesForUser.get(0));
        return save(newUser);
    }

    @Transactional
    public User save(User user) {
        return repository.save(user);
    }

    private void createStartKindsForUser(User user) {
        UUID userGroup = user.getUserGroup();
        for (HashMap.Entry<String, Type> entry : mapStartKinds.entrySet()) {
            kindRepository.save(new Kind(entry.getValue(), entry.getKey(), userGroup));
        }
    }

    private List<Currency> createStartCurrenciesForUser(User user) {
        List<Currency> currencies = new ArrayList<>();
        UUID userGroup = user.getUserGroup();
        for (String currencyName : listStartCurrencies) {
            currencies.add(currencyRepository.save(new Currency(currencyName, userGroup)));
        }
        return currencies;
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    public List<User> findByUserGroup(UUID userGroup) {
        return repository.findByUserGroup(userGroup);
    }

    @Override
    public User findByEmail(String email) {
        return repository.getByEmail(email);
    }

    @Override
    public User findById(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    @Override
    public User changeDefaultCurrency(UUID currencyId) {
        Currency currency = currencyRepository.findById(currencyId);
        if (currency == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User user = SecurityUtil.get().getUser();
        user.setCurrencyDefault(currency);
        return this.save(user);
    }
}




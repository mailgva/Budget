package com.gorbatenko.budget.util;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.Currency;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static java.util.Objects.requireNonNull;

public class SecurityUtil {

    private SecurityUtil() {
    }

    private static AuthorizedUser safeGet() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return (principal instanceof AuthorizedUser) ? (AuthorizedUser) principal : null;
    }

    public static AuthorizedUser get() {
        AuthorizedUser user = safeGet();
        requireNonNull(user, "No authorized user found");
        return user;
    }

    public static String authUserName() {
        return get().getUser().getName();
    }

    public static Currency getCurrencyDefault() {
        return get().getUser().getCurrencyDefault();
    }

    public static String getUserGroup() {
        return SecurityUtil.get().getUser().getGroup();
    }

}
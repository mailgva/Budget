package com.gorbatenko.budget.util;

import static java.util.Objects.requireNonNull;

import com.gorbatenko.budget.AuthorizedUser;
import com.gorbatenko.budget.model.Budget;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static AuthorizedUser safeGet() {
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

    public static String authUserEmail() {
        return get().getUsername();
    }

    public static List<Budget> hidePassword(List<Budget> budgetList) {
        return budgetList.stream()
                .map(budget -> {
                    budget.getUser().setPassword("");
                    return budget;
                })
                .collect(Collectors.toList());
    }

}
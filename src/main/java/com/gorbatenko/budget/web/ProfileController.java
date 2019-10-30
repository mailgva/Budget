package com.gorbatenko.budget.web;

import com.gorbatenko.budget.model.Role;
import com.gorbatenko.budget.model.User;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/profile/")
public class ProfileController extends AbstractWebController {

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model) {
        User user = SecurityUtil.get().getUser();

        List<User> usersGroup = userService.getByGroup(user.getGroup());
        String groupMembers = usersGroup.stream()
                .map(u -> u.getName())
                .collect(Collectors.joining(","));

        model.addAttribute("user", user);
        model.addAttribute("groupMembers", groupMembers);
        return "profile/profile";
    }

    @GetMapping("/register")
    public String register() {
        return "/profile/register";
    }

    @PostMapping("/register")
    public String newUser(@ModelAttribute User user, Model model) throws Exception{
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        try {
            userService.create(user);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "/profile/register";
        }
        return "redirect:/login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/jointogroup/{id}")
    public String joinToGroup(@PathVariable("id") String id) {
        User user = SecurityUtil.get().getUser();
        user.setGroup(id);
        userService.save(user);
        return "profile/profile";
    }
}

package ru.andrev.authservice.controller;

import ru.andrev.authservice.model.User;
import ru.andrev.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public User register(
            @RequestParam String name,
            @RequestParam String login,
            @RequestParam String password) {
        return authService.register(name, login, password);
    }

    @PostMapping("/login")
    public User login(
            @RequestParam String login,
            @RequestParam String password) {
        return authService.login(login, password);
    }
}
package ru.andrev.familytreeservice.controller;

import ru.andrev.familytreeservice.model.User;
import ru.andrev.familytreeservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/treecompare")
    public List<User> compareFamilyTrees(@RequestParam("id") List<Long> familyTreeIds) {
        return userService.compareFamilyTrees(familyTreeIds);
    }
}
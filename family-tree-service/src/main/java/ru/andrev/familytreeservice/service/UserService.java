package ru.andrev.familytreeservice.service;

import ru.andrev.familytreeservice.model.User;
import ru.andrev.familytreeservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> compareFamilyTrees(List<Long> familyTreeIds) {
        return userRepository.findAll();
    }
}
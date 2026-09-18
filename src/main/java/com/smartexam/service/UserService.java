package com.smartexam.service;

import com.smartexam.dto.UserRegistrationDto;
import com.smartexam.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(UserRegistrationDto registrationDto);
    User createUser(User user, String roleName);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    List<User> findAllStudents();
    List<User> findAllFaculty();
    List<User> findAllUsers();
    User toggleUserStatus(Long id);
    void deleteUser(Long id);
}

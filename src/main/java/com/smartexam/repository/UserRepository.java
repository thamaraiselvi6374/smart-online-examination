package com.smartexam.repository;

import com.smartexam.entity.User;
import com.smartexam.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleName(String roleName);
    long countByRoleName(String roleName);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

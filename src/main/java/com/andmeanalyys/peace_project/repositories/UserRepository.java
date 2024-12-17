package com.andmeanalyys.peace_project.repositories;

import com.andmeanalyys.peace_project.records.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsernameAndEmail(String username, String email);

    Optional<User> findByEmail(String email);
}
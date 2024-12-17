package com.andmeanalyys.peace_project.services;

import com.andmeanalyys.peace_project.dto.UserDTO;
import com.andmeanalyys.peace_project.records.User;
import com.andmeanalyys.peace_project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Finds a user by username and email.
     * If the user does not exist, creates a new user.
     *
     * @param username The username of the user.
     * @param email The email of the user.
     * @return The existing or newly created user.
     */
    public User findOrCreateUser(String username, String email) {
        Optional<User> optionalUser = userRepository.findByUsernameAndEmail(username, email);
        return optionalUser.orElseGet(() -> {
            User newUser = User.builder()
                    .id(UUID.randomUUID())
                    .username(username)
                    .email(email)
                    .role("USER")
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        });
    }

    public static UserDTO convertToUserDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}

package com.andmeanalyys.peace_project.controllers;

import com.andmeanalyys.peace_project.dto.LoginRequest;
import com.andmeanalyys.peace_project.records.User;
import com.andmeanalyys.peace_project.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        User user = userService.findOrCreateUser(loginRequest.getUsername(), loginRequest.getEmail());

        // Create an authentication token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, // Principal
                null, // Credentials (not used)
                user.getAuthorities() // Authorities
        );

        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create a session and set the security context
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        return ResponseEntity.ok("User authenticated successfully");
    }
}

package com.andmeanalyys.peace_project.security;

import com.andmeanalyys.peace_project.records.User;
import com.andmeanalyys.peace_project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String email = (String) authentication.getCredentials();

        // Validate user based on username and email
        User user = userRepository.findByUsernameAndEmail(username, email)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or email"));

        // Create an authenticated token with user details and authorities
        return new UsernamePasswordAuthenticationToken(
                user, // Principal
                null, // Credentials (not used)
                user.getAuthorities() // Authorities
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

package com.andmeanalyys.peace_project.config;

import com.andmeanalyys.peace_project.security.CustomAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;

    public SecurityConfig(CustomAuthenticationProvider customAuthenticationProvider) {
        this.customAuthenticationProvider = customAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for simplicity; enable in production
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/login/**", "/css/**", "/js/**").permitAll() // Permit login endpoints
                        .anyRequest().authenticated() // Require authentication for all other requests
                )
                .authenticationProvider(customAuthenticationProvider) // Register custom provider
                .formLogin(form -> form
                        .loginPage("/login/index.html") // Specify custom login page
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll
                );

        return http.build();
    }
}

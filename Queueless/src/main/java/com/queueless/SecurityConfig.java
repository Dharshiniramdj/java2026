package com.queueless;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(Repositories.Users users) {
        return email -> {
            var u = users.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            return User.withUsername(u.email)
                .password(u.passwordHash)
                .roles(u.role)
                .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // allow H2 console if needed
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html", "/style.css", "/app.js",
                    "/api/register", "/api/services", "/api/counters",
                    "/ws/**", "/h2-console/**", "/favicon.ico"
                ).permitAll()
                .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
}

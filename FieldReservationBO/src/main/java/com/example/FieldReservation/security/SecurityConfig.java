package com.example.FieldReservation.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // modern way to disable CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()   // allow all requests
                )
                .httpBasic(Customizer.withDefaults());  // optional: enable basic auth for testing in browser
        return http.build();
    }
}

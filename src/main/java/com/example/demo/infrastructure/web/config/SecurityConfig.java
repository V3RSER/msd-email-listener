package com.example.demo.infrastructure.web.config;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.time.Instant;
import java.time.ZoneId;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserConnectionRepository userConnectionRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/webhooks/outlook").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(authenticationSuccessHandler()))
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .logoutSuccessHandler(logoutSuccessHandler()));
        return http.build();
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                OAuth2User user = oauth2Token.getPrincipal();

                UserConnection userConnection = new UserConnection();
                userConnection.setUserId(user.getName());

                // You should securely retrieve and store the access and refresh tokens
                // For simplicity, we are not doing it here.
                userConnection.setAccessToken("dummy-access-token");
                userConnection.setRefreshToken("dummy-refresh-token");
                userConnection.setTokenExpiration(Instant.now().plusSeconds(3600).atZone(ZoneId.systemDefault()).toOffsetDateTime());

                userConnectionRepository.save(userConnection);
            }
            response.sendRedirect("/");
        };
    }

    private LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            // Here, you would typically invalidate the user's session and tokens.
            // For this example, we'll just redirect to the homepage.
            response.sendRedirect("/");
        };
    }
}

package com.example.demo.infrastructure.web.config;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final UserConnectionRepository userConnectionRepository;

    public SecurityConfig(UserConnectionRepository userConnectionRepository) {
        this.userConnectionRepository = userConnectionRepository;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/v1/webhooks/outlook", "/favicon.ico").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authenticationSuccessHandler(authenticationSuccessHandler())
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
            );

        return http.build();
    }

    private ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        RedirectServerAuthenticationSuccessHandler redirectHandler = new RedirectServerAuthenticationSuccessHandler("/api/v1/auth/status");

        return (WebFilterExchange webFilterExchange, Authentication authentication) -> {
            if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
                OAuth2User user = oauth2Token.getPrincipal();

                // Note: The logic here should be updated to properly retrieve access and refresh tokens
                // from the reactive OAuth2AuthorizedClientService if needed.
                // Right now it just demonstrates saving dummy data as it did before.
                
                return userConnectionRepository.findByUserId(user.getName())
                    .defaultIfEmpty(new UserConnection())
                    .flatMap(userConnection -> {
                        userConnection.setUserId(user.getName());
                        userConnection.setAccessToken("dummy-access-token");
                        userConnection.setRefreshToken("dummy-refresh-token");
                        userConnection.setTokenExpiration(Instant.now().plusSeconds(3600).atZone(ZoneId.systemDefault()).toOffsetDateTime());
                        
                        return userConnectionRepository.save(userConnection);
                    })
                    .then(redirectHandler.onAuthenticationSuccess(webFilterExchange, authentication));
            }
            
            return redirectHandler.onAuthenticationSuccess(webFilterExchange, authentication);
        };
    }
}

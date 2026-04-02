package com.example.demo.infrastructure.web.config;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final UserConnectionRepository userConnectionRepository;
    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

    public SecurityConfig(UserConnectionRepository userConnectionRepository, ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.userConnectionRepository = userConnectionRepository;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v1/webhooks/**", "/favicon.ico").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(authenticationSuccessHandler())
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable); // Not recommended for production
        return http.build();
    }

    @Bean
    public ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new RedirectServerAuthenticationSuccessHandler() {
            @Override
            public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
                if (authentication instanceof OAuth2AuthenticationToken) {
                    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                    String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();

                    return authorizedClientRepository.loadAuthorizedClient(clientRegistrationId, authentication, webFilterExchange.getExchange())
                        .flatMap(authorizedClient -> {
                            OAuth2User user = oauthToken.getPrincipal();
                            String providerId = user.getName();
                            String email = user.getAttribute("email");
                            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                            OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

                            return userConnectionRepository.findByProviderAndProviderId(clientRegistrationId, providerId)
                                .flatMap(existingConnection -> {
                                    // User exists, update tokens
                                    existingConnection.setAccessToken(accessToken.getTokenValue());
                                    if (refreshToken != null) {
                                        existingConnection.setRefreshToken(refreshToken.getTokenValue());
                                    }
                                    existingConnection.setExpiresIn(accessToken.getExpiresAt() != null ? LocalDateTime.ofInstant(accessToken.getExpiresAt(), ZoneId.systemDefault()) : null);
                                    existingConnection.setUpdatedAt(LocalDateTime.now());
                                    return userConnectionRepository.save(existingConnection);
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    // New user, create connection
                                    UserConnection newUserConnection = new UserConnection(
                                            null, // id is auto-generated
                                            clientRegistrationId,
                                            providerId,
                                            email,
                                            accessToken.getTokenValue(),
                                            refreshToken != null ? refreshToken.getTokenValue() : null,
                                            accessToken.getExpiresAt() != null ? LocalDateTime.ofInstant(accessToken.getExpiresAt(), ZoneId.systemDefault()) : null,
                                            LocalDateTime.now(),
                                            LocalDateTime.now()
                                    );
                                    return userConnectionRepository.save(newUserConnection);
                                }))
                                .then(super.onAuthenticationSuccess(webFilterExchange, authentication));
                        });
                }
                return super.onAuthenticationSuccess(webFilterExchange, authentication);
            }
        };
    }
}

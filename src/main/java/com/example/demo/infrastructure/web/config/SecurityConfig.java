package com.example.demo.infrastructure.web.config;

import com.example.demo.application.service.OAuth2LoginSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessService oAuth2LoginSuccessService;
    private final CustomReactiveOAuth2AuthorizedClientService customReactiveOAuth2AuthorizedClientService;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v1/webhooks/outlook", "/favicon.ico").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizedClientService(customReactiveOAuth2AuthorizedClientService)
                        .authenticationSuccessHandler(authenticationSuccessHandler())
                )
                .logout(logout -> logout.logoutUrl("/logout"));

        return http.build();
    }

    private ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        RedirectServerAuthenticationSuccessHandler redirectHandler = new RedirectServerAuthenticationSuccessHandler("/api/v1/auth/status");

        return (webFilterExchange, authentication) -> {
            if (authentication instanceof OAuth2AuthenticationToken) {
                return oAuth2LoginSuccessService.onAuthenticationSuccess(webFilterExchange.getExchange(), (OAuth2AuthenticationToken) authentication)
                        .then(redirectHandler.onAuthenticationSuccess(webFilterExchange, authentication));
            }
            return redirectHandler.onAuthenticationSuccess(webFilterExchange, authentication);
        };
    }
}

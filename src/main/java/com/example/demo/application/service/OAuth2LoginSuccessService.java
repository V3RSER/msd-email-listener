package com.example.demo.application.service;

import com.example.demo.domain.service.OutlookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessService {

    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
    private final OutlookService outlookService;

    public Mono<Void> onAuthenticationSuccess(ServerWebExchange exchange, OAuth2AuthenticationToken authentication) {
        log.info("Authentication successful for user: {}", authentication.getName());
        return authorizedClientRepository.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication, exchange)
                .switchIfEmpty(Mono.error(new IllegalStateException("No authorized client found after login. The client should have been saved automatically.")))
                .flatMap(authorizedClient -> {
                    log.info("Creating email subscription for user: {}", authorizedClient.getPrincipalName());
                    return outlookService.createEmailSubscription(authorizedClient.getPrincipalName());
                })
                .doOnSuccess(aVoid -> log.info("Successfully processed authentication and created subscription for user: {}", authentication.getName()))
                .doOnError(throwable -> log.error("Error during authentication success processing for user: {}", authentication.getName(), throwable))
                .then();
    }
}

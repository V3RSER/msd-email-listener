package com.example.demo.application.service;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.domain.service.OutlookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OAuth2LoginSuccessService {

    private final UserConnectionRepository userConnectionRepository;
    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
    private final OutlookService outlookService;

    public Mono<Void> onAuthenticationSuccess(ServerWebExchange exchange, OAuth2AuthenticationToken authentication) {
        return authorizedClientRepository.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication, exchange)
                .flatMap(this::saveUserConnection)
                .flatMap(userConnection -> outlookService.createEmailSubscription(userConnection.getUserId()))
                .then();
    }

    private Mono<UserConnection> saveUserConnection(OAuth2AuthorizedClient authorizedClient) {
        String userId = authorizedClient.getPrincipalName();
        String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();
        String refreshTokenValue = authorizedClient.getRefreshToken() != null ? authorizedClient.getRefreshToken().getTokenValue() : null;
        Instant issuedAt = authorizedClient.getAccessToken().getIssuedAt();
        Instant expiresAt = authorizedClient.getAccessToken().getExpiresAt();

        return userConnectionRepository.findByUserId(userId)
                .defaultIfEmpty(new UserConnection())
                .flatMap(userConnection -> {
                    userConnection.setUserId(userId);
                    userConnection.setAccessToken(accessTokenValue);
                    userConnection.setRefreshToken(refreshTokenValue);
                    userConnection.setAccessTokenIssuedAt(issuedAt);
                    userConnection.setAccessTokenExpiresAt(expiresAt);
                    return userConnectionRepository.save(userConnection);
                });
    }
}

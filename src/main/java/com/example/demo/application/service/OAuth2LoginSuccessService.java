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

import java.time.ZoneOffset;

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
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String refreshToken = authorizedClient.getRefreshToken() != null ? authorizedClient.getRefreshToken().getTokenValue() : null;
        var tokenExpiration = authorizedClient.getAccessToken().getExpiresAt() != null ?
                authorizedClient.getAccessToken().getExpiresAt().atOffset(ZoneOffset.UTC) : null;

        return userConnectionRepository.findByUserId(userId)
                .defaultIfEmpty(new UserConnection())
                .flatMap(userConnection -> {
                    userConnection.setUserId(userId);
                    userConnection.setAccessToken(accessToken);
                    userConnection.setRefreshToken(refreshToken);
                    userConnection.setTokenExpiration(tokenExpiration);
                    return userConnectionRepository.save(userConnection);
                });
    }
}

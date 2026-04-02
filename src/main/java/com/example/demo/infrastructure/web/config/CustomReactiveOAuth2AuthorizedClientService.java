package com.example.demo.infrastructure.web.config;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class CustomReactiveOAuth2AuthorizedClientService implements ReactiveOAuth2AuthorizedClientService {

    private final UserConnectionRepository userConnectionRepository;
    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Override
    public <T extends OAuth2AuthorizedClient> Mono<T> loadAuthorizedClient(String clientRegistrationId, String principalName) {
        return clientRegistrationRepository.findByRegistrationId(clientRegistrationId)
                .flatMap(clientRegistration -> userConnectionRepository.findByUserId(principalName)
                        .filter(user -> user.getAccessToken() != null)
                        .map(user -> {
                            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                                    OAuth2AccessToken.TokenType.BEARER,
                                    user.getAccessToken(),
                                    user.getAccessTokenIssuedAt().toInstant(),
                                    user.getAccessTokenExpiresAt().toInstant()
                            );
                            return (T) new OAuth2AuthorizedClient(clientRegistration, principalName, accessToken);
                        }));
    }

    @Override
    public Mono<Void> saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication authentication) {
        String userId = authorizedClient.getPrincipalName();
        String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();
        String refreshTokenValue = authorizedClient.getRefreshToken() != null ? authorizedClient.getRefreshToken().getTokenValue() : null;
        Instant issuedAt = authorizedClient.getAccessToken().getIssuedAt();
        Instant expiresAt = authorizedClient.getAccessToken().getExpiresAt();

        return userConnectionRepository.findByUserId(userId)
                .defaultIfEmpty(new UserConnection(userId))
                .flatMap(userConnection -> {
                    userConnection.setAccessToken(accessTokenValue);
                    userConnection.setRefreshToken(refreshTokenValue);
                    userConnection.setAccessTokenIssuedAt(issuedAt.atOffset(ZoneOffset.UTC));
                    userConnection.setAccessTokenExpiresAt(expiresAt.atOffset(ZoneOffset.UTC));
                    return userConnectionRepository.save(userConnection);
                }).then();
    }

    @Override
    public Mono<Void> removeAuthorizedClient(String clientRegistrationId, String principalName) {
        // Implementation can be added if needed, for example on logout.
        return Mono.empty();
    }
}

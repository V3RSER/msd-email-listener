package com.example.demo.infrastructure.web.config;

import com.example.demo.domain.repository.UserConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
        // This is handled by OAuth2LoginSuccessService, so this is a no-op.
        return Mono.empty();
    }

    @Override
    public Mono<Void> removeAuthorizedClient(String clientRegistrationId, String principalName) {
        // Implementation can be added if needed, for example on logout.
        return Mono.empty();
    }
}

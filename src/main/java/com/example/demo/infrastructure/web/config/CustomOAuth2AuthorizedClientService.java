package com.example.demo.infrastructure.web.config;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

    private final UserConnectionRepository userConnectionRepository;

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        UserConnection user = userConnectionRepository.findByUserId(principalName).block();
        if (user != null && user.getAccessToken() != null) {
            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    user.getAccessToken(),
                    Instant.now(), // This is not ideal, but we don't store the expiry
                    user.getAccessTokenExpiresAt()
            );

            return (T) new OAuth2AuthorizedClient(
                    new org.springframework.security.oauth2.client.registration.ClientRegistration.Builder(clientRegistrationId)
                            .build(),
                    principalName,
                    accessToken
            );
        }
        return null;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication authentication) {
        // This is handled by OAuth2LoginSuccessService
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        // Implementation can be added if needed
    }
}

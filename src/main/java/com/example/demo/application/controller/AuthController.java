package com.example.demo.application.controller;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.msgraph.MicrosoftGraphClient;
import com.microsoft.graph.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserConnectionRepository userConnectionRepository;
    private final MicrosoftGraphClient microsoftGraphClient;

    @GetMapping("/me")
    public Mono<User> getCurrentUser(
            @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String refreshToken = authorizedClient.getRefreshToken() != null ? authorizedClient.getRefreshToken().getTokenValue() : null;

        return microsoftGraphClient.getUserFromGraph(accessToken)
                .flatMap(user -> userConnectionRepository.findByUserId(user.getId())
                        .defaultIfEmpty(new UserConnection())
                        .flatMap(connection -> {
                            connection.setUserId(user.getId());
                            connection.setAccessToken(accessToken);
                            connection.setRefreshToken(refreshToken);
                            return userConnectionRepository.save(connection);
                        })
                        .thenReturn(user));
    }
}

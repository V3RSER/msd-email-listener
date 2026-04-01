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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserConnectionRepository userConnectionRepository;
    private final MicrosoftGraphClient microsoftGraphClient;

    @GetMapping("/me")
    public User getCurrentUser(
            @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String principalName = oauth2User.getName(); // This is the user's principal name

        User user = microsoftGraphClient.getUserFromGraph(accessToken);

        // Storing or updating the user connection details
        UserConnection connection = userConnectionRepository.findByUserId(user.id)
                .orElse(new UserConnection());
        connection.setUserId(user.id);
        connection.setAccessToken(accessToken);
        connection.setRefreshToken(authorizedClient.getRefreshToken().getTokenValue());
        userConnectionRepository.save(connection);

        return user;
    }
}

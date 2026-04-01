package com.example.demo.application.controller;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${azure.client-id}")
    private String clientId;
    @Value("${azure.client-secret}")
    private String clientSecret;
    @Value("${azure.tenant-id}")
    private String tenantId;
    @Value("${azure.redirect-uri}")
    private String redirectUri;

    private final UserConnectionRepository userConnectionRepository;

    public AuthController(UserConnectionRepository userConnectionRepository) {
        this.userConnectionRepository = userConnectionRepository;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        String authorizationUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/authorize?"
                + "client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + redirectUri
                + "&response_mode=query"
                + "&scope=openid%20profile%20offline_access%20Mail.Read"
                + "&state=" + UUID.randomUUID();
        return ResponseEntity.status(302).header("Location", authorizationUrl).build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state) {
        AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .redirectUrl(redirectUri)
                .authorizationCode(code)
                .build();

        // The get-token logic will exchange the auth code for an access token and refresh token
        var tokenResponse = credential.getTokenSync(new com.azure.core.credential.TokenRequestContext().addScopes("Mail.Read"));

        // For simplicity, we're not using the Graph API to get the user's ID here.
        // We're assuming the user's ID from the token is sufficient.
        // In a real app, you'd get the user's profile.
        UserConnection userConnection = new UserConnection();
        // This is not the real user ID, but we don't have it at this point
        // without another Graph API call.
        userConnection.setUserId("me"); 
        userConnection.setAccessToken(tokenResponse.getToken());
        userConnection.setRefreshToken("dummy-refresh-token"); // Placeholder
        userConnection.setTokenExpiration(tokenResponse.getExpiresAt());

        userConnectionRepository.save(userConnection);

        return ResponseEntity.ok("Authentication successful! You can close this window.");
    }
}

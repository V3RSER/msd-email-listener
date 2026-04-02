package com.example.demo.infrastructure.web.controller;

import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.web.dto.AuthStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserConnectionRepository userConnectionRepository;

    @GetMapping("/status")
    public Mono<ResponseEntity<AuthStatusResponse>> getStatus(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            AuthStatusResponse response = new AuthStatusResponse(null, false, "Not authenticated");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
        }

        String userId = oauth2User.getName();
        return userConnectionRepository.findByUserId(userId)
                .map(userConnection -> {
                    AuthStatusResponse response = new AuthStatusResponse(userId, true, "User is connected to Outlook.");
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.ok(new AuthStatusResponse(userId, false, "User is not connected to Outlook.")));
    }
}

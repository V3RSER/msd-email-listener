package com.example.demo.application.controller;

import com.example.demo.domain.repository.UserConnectionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserConnectionRepository userConnectionRepository;

    public AuthController(UserConnectionRepository userConnectionRepository) {
        this.userConnectionRepository = userConnectionRepository;
    }

    @GetMapping("/status")
    public Mono<ResponseEntity<String>> getStatus(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return Mono.just(ResponseEntity.status(401).body("Not authenticated"));
        }

        return userConnectionRepository.findByUserId(oauth2User.getName())
                .map(userConnection -> ResponseEntity.ok("User " + userConnection.getUserId() + " is connected to Outlook."))
                .defaultIfEmpty(ResponseEntity.status(404).body("User " + oauth2User.getName() + " is not connected to Outlook."));
    }
}

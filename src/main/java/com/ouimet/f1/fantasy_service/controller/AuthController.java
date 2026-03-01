package com.ouimet.f1.fantasy_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(@AuthenticationPrincipal OAuth2User oauthUser) {
        if (oauthUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthMeResponse(null, null, null, null, false));
        }
        
        String email = oauthUser.getAttribute("email");
        String fullName = oauthUser.getAttribute("name");
        String pictureUrl = oauthUser.getAttribute("picture");

        AuthMeResponse response = new AuthMeResponse(
                oauthUser.getName(),
                email,
                fullName,
                pictureUrl,
                true);

        return ResponseEntity.ok(response);
    }

    public record AuthMeResponse(
            String id,
            String email,
            String name,
            String picture,
            boolean authenticated) {
    }
}

package com.ouimet.f1.fantasy_service.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResult {
    private boolean success = false;
    private String sessionId;
    private String subscriptionToken;
    private String subscriberId;
    private String email;
}

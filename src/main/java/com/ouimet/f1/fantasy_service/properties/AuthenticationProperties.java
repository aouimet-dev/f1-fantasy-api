package com.ouimet.f1.fantasy_service.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "f1.fantasy")
public class AuthenticationProperties {
    /**
     * Nom d'utilisateur (email) pour l'authentification
     */
    private String username;

    /**
     * Mot de passe pour l'authentification
     */
    private String password;

}

// package com.ouimet.f1.fantasy_service.client;

// import java.time.Instant;
// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.Base64;
// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Component;
// import org.springframework.web.client.RestTemplate;

// import com.ouimet.f1.fantasy_service.dto.LoginSessionDTO;
// import com.ouimet.f1.fantasy_service.exception.F1FantasyException;
// import com.ouimet.f1.fantasy_service.properties.AuthenticationProperties;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import tools.jackson.databind.JsonNode;
// import tools.jackson.databind.ObjectMapper;

// /**
//  * Client principal pour l'API F1 Fantasy
//  * Basé sur https://github.com/zeroclutch/f1-fantasy-api
//  */
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class F1FantasyClient {

//     private static final String AUTH_URL = "https://api.formula1.com/v2/account/subscriber/authenticate/by-password";
//     private static final String PLAYON_URL = "https://play-on.formula1.com/api/identity/playon-session";

//     private final RestTemplate restTemplate;
//     private final ObjectMapper objectMapper;
//     private final AuthenticationProperties authenticationProperties;

//     private String cookieString;
//     private LoginSessionDTO loginSession;
//     private LocalDateTime loginSessionExpiry;
//     private Long userId;

//     /**
//      * Initialise le client avec authentification et chargement des données
//      */
//     public F1FantasyClient init() throws F1FantasyException {
//         log.info("Initialisation du client F1 Fantasy...");

//         login(authenticationProperties.getUsername(), authenticationProperties.getPassword());

//         log.info("Client F1 Fantasy initialisé avec succès");
//         return this;
//     }

//     /**
//      * Connexion avec username et password
//      */
//     public void login(String username, String password) throws F1FantasyException {
//         try {
//             log.info("Connexion en cours pour l'utilisateur: {}", username);

//             // Étape 1: Authentification
//             LoginSessionDTO session = loginWithPassword(username, password);
//             this.loginSession = session;

//             // Étape 2: Créer la session PlayOn
//             String playOnCookie = getPlayOnSession(session);
//             this.cookieString = playOnCookie;

//             log.info("Connexion réussie. User ID: {}", userId);
//         } catch (Exception e) {
//             throw new F1FantasyException("Erreur lors de la connexion: " + e.getMessage(), e);
//         }
//     }

//     /**
//      * Authentification avec password
//      */
//     private LoginSessionDTO loginWithPassword(String username, String password) throws F1FantasyException {
//         Map<String, String> body = new HashMap<>();
//         body.put("Login", username);
//         body.put("Password", password);

//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         headers.set("User-Agent", "RaceControl API");

//         HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

//         ResponseEntity<JsonNode> response = restTemplate.postForEntity(AUTH_URL, request, JsonNode.class);

//         if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//             JsonNode data = response.getBody().get("data");
//             String subscriptionToken = data.get("subscriptionToken").asString();

//             // Décoder le JWT pour obtenir l'expiration et l'ID utilisateur
//             String[] jwtParts = decodeJWT(subscriptionToken);
//             JsonNode payload = objectMapper.readTree(jwtParts[1]);

//             long exp = payload.get("exp").asLong();
//             this.loginSessionExpiry = LocalDateTime.ofInstant(
//                     Instant.ofEpochSecond(exp),
//                     ZoneId.systemDefault());

//             this.userId = payload.get("data").get("subscriptionId").asLong();

//             LoginSessionDTO session = new LoginSessionDTO();
//             session.setSubscriptionToken(subscriptionToken);
//             session.setData(data);

//             return session;
//         }

//         throw new F1FantasyException("Échec de l'authentification");
//     }

//     /**
//      * Crée une session PlayOn pour les requêtes authentifiées
//      */
//     private String getPlayOnSession(LoginSessionDTO session) throws Exception {
//         Map<String, String> body = new HashMap<>();
//         body.put("identity_provider_url", "https://api.formula1.com/v2/account/subscriber");
//         body.put("access_token", session.getSubscriptionToken());

//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);

//         HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

//         ResponseEntity<JsonNode> response = restTemplate.postForEntity(PLAYON_URL, request, JsonNode.class);

//         if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
//             String sessionId = response.getBody().get("session_id").asString();
//             return "play-on-session=" + sessionId;
//         }

//         throw new F1FantasyException("Échec de la création de la session PlayOn");

//     }

//     /**
//      * Décode un JWT
//      */
//     private String[] decodeJWT(String jwt) {
//         String[] parts = jwt.split("\\.");
//         return new String[] {
//                 new String(Base64.getUrlDecoder().decode(parts[0])),
//                 new String(Base64.getUrlDecoder().decode(parts[1])),
//                 parts[2]
//         };
//     }

//     /**
//      * Vérifie si l'utilisateur est authentifié
//      */
//     public boolean isAuthenticated() {
//         return cookieString != null &&
//                 loginSession != null &&
//                 loginSessionExpiry != null &&
//                 loginSessionExpiry.isAfter(LocalDateTime.now());
//     }

// }
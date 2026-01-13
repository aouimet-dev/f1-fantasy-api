// ============================================
// AuthenticationClient.java
// ============================================
package com.ouimet.f1.fantasy_service.client;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AuthenticationClient {

    private final WebClient webClient;

    public AuthenticationClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.formula1.com")
                .build();
    }

    public Mono<String> getReese84CookieWithHeaders() {
        Formula1Request request = createRequest();

        return webClient.post()
                .uri("/6657193977244c13?d=account.formula1.com")
                .bodyValue(request)
                .retrieve()
                .toEntity(String.class)
                .map(response -> {
                    List<String> cookies = response.getHeaders().get("Set-Cookie");
                    if (cookies != null) {
                        return cookies.stream()
                                .filter(cookie -> cookie.startsWith("reese84="))
                                .findFirst()
                                .map(cookie -> cookie.split(";")[0].replace("reese84=", ""))
                                .orElseThrow(() -> new RuntimeException("Cookie reese84 non trouvé"));
                    }
                    throw new RuntimeException("Aucun cookie dans la réponse");
                });
    }

    public Mono<String> authenticateByPasswordWithHeaders(String reese84Cookie, String username, String password) {
        LoginRequest loginRequest = new LoginRequest(
                "d861e38f-05ea-4063-8776-a7e2b6d885a4",
                username,
                password);

        String cookieValue = String.format(
                "reese84=%s; login={\"event\":\"login\",\"componentId\":\"component_login_page\",\"actionType\":\"failed\"}",
                reese84Cookie);

        return webClient.post()
                .uri("/v2/account/subscriber/authenticate/by-password")
                .header("apiKey", "fCUCjWrKPu9ylJwRAv8BpGLEgiAuThx7")
                .header("Cookie", cookieValue)
                .bodyValue(loginRequest)
                .retrieve()
                .toEntity(String.class)
                .map(response -> {
                    // Option 1 : Si c'est dans un header
                    String xF1Cookie = response.getHeaders().getFirst("X-F1-Cookie-Data");
                    if (xF1Cookie != null) {
                        return xF1Cookie;
                    }

                    // Option 2 : Si c'est dans le body JSON, vous devrez parser ici
                    // Par exemple avec Jackson :
                    // ObjectMapper mapper = new ObjectMapper();
                    // JsonNode json = mapper.readTree(response.getBody());
                    // return json.get("data").get("subscriptionToken").asText();

                    throw new RuntimeException("X-F1-Cookie-Data non trouvé");
                });
    }

    public Mono<String> getLeagueHistory(String leagueId, String xF1CookieData) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("fantasy-api.formula1.com")
                        .path("/partner_games/f1/leaderboards/league/history")
                        .queryParam("league_id", leagueId)
                        .queryParam("slot", 2)
                        .queryParam("type", "league")
                        .build())
                .header("Referer", "")
                .header("X-F1-Cookie-Data", xF1CookieData)
                .retrieve()
                .bodyToMono(String.class);
    }

    // Méthodes originales (pour rétrocompatibilité si besoin)
    public Mono<String> getReese84Cookie() {
        Formula1Request request = createRequest();

        return webClient.post()
                .uri("/6657193977244c13?d=account.formula1.com")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> authenticateByPassword(String reese84Cookie, String username, String password) {
        LoginRequest loginRequest = new LoginRequest(
                "d861e38f-05ea-4063-8776-a7e2b6d885a4",
                username,
                password);

        String cookieValue = String.format(
                "reese84=%s; login={\"event\":\"login\",\"componentId\":\"component_login_page\",\"actionType\":\"failed\"}",
                reese84Cookie);

        return webClient.post()
                .uri("/v2/account/subscriber/authenticate/by-password")
                .header("apiKey", "fCUCjWrKPu9ylJwRAv8BpGLEgiAuThx7")
                .header("Cookie", cookieValue)
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(String.class);
    }

    private Formula1Request createRequest() {
        Interrogation interrogation = new Interrogation(162229509L, 1959639815L, 78830557L);
        Solution solution = new Solution(interrogation, "stable");
        return new Formula1Request(solution, null, new Performance(185));
    }

    public record Formula1Request(Solution solution, Object error, Performance performance) {
    }

    public record Solution(Interrogation interrogation, String version) {
    }

    public record Interrogation(Long st, Long sr, Long cr) {
    }

    public record Performance(Integer interrogation) {
    }

    public record LoginRequest(String DistributionChannel, String Login, String Password) {
    }
}

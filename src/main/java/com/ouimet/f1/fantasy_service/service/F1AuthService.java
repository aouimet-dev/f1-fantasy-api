package com.ouimet.f1.fantasy_service.service;

import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.ouimet.f1.fantasy_service.domain.AuthenticationResult;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class F1AuthService {
    private static final String F1_LOGIN_URL = "https://account.formula1.com";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationResult authenticate(String email, String password) {
        AuthenticationResult result = new AuthenticationResult();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright
                    .chromium()
                    .launch(
                            new BrowserType.LaunchOptions()
                                    .setHeadless(false)
                                    .setSlowMo(150)
                                    .setDevtools(true));

            BrowserContext context = browser
                    .newContext(
                            new Browser.NewContextOptions()
                                    .setUserAgent(
                                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36")
                                    .setViewportSize(1920, 1080));

            Page page = context.newPage();

            // Intercepter la réponse d'authentification
            page.onResponse(response -> {
                if (response.url().contains("authenticate/by-password") && response.status() == 200) {
                    try {
                        String body = response.text();
                        log.info("Réponse d'authentification reçue : {}", body);

                        JsonNode json = objectMapper.readTree(body);

                        result.setSessionId(json.get("SessionId").asString());
                        result.setSubscriptionToken(json.path("data").path("subscriptionToken").asString());
                        result.setSubscriberId(json.path("Subscriber").path("Id").asString());
                        result.setEmail(json.path("Subscriber").path("Email").asString());
                        result.setSuccess(true);
                        log.info("Authentification réussie");

                    } catch (Exception e) {
                        result.setSuccess(false);
                        log.error("Erreur en traitant la réponse d'authentification : {}", e.getMessage());
                    }
                }
            });

            // Aller sur la page de login
            log.info("Navigation vers https://account.formula1.com");
            page.navigate(F1_LOGIN_URL);
            page.waitForLoadState();

            Frame consentFrame = page.frameByUrl(url -> url.contains("consent.formula1.com"));
            if (consentFrame != null) {
                log.info("Un cadre de consentement a été trouvé. Acceptation des cookies");
                consentFrame.getByRole(
                        AriaRole.BUTTON,
                        new Frame.GetByRoleOptions().setName("TOUT ACCEPTER")).click();
                page.waitForSelector(
                        "div[id^='sp_message_container']",
                        new Page.WaitForSelectorOptions()
                                .setState(WaitForSelectorState.DETACHED));
            }

            log.info("En attente de détection du champ text pour le email");
            page.waitForSelector("input[type='text'], input[name='Login']",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));

            log.info("Remplissage des informations d'authentification");
            page.click("input[name='Login']");
            page.type("input[name='Login']", email, new Page.TypeOptions().setDelay(50));

            page.click("input[name='Password']");
            page.type("input[name='Password']", password, new Page.TypeOptions().setDelay(50));
            
            // Bouton login
            Locator loginButton = page.getByRole(
                    AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Connectez-vous"));

            // attendre qu’il soit réellement activé par JS
            loginButton.waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE));

            log.info("Soumission du formulaire d'authentification");

            // Click + attente API
            page.waitForResponse(
                    resp -> resp.url().contains("authenticate/by-password"),
                    () -> loginButton.click());
            
            browser.close();

        } catch (Exception e) {
            result.setSuccess(false);
            log.error("Erreur lors de l'authentification : {}", e.getMessage());
        }

        return result;
    }
}

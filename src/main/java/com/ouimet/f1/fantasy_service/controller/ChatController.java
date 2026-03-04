package com.ouimet.f1.fantasy_service.controller;

import java.util.Map;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ai")
@RestController
public class ChatController {

    private final OllamaChatModel chatModel;

    public ChatController(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/generate")
    public Map<String, String> generate(@RequestParam String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

}

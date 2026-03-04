package com.ouimet.f1.fantasy_service.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/chat")
@RestController
public class OllamaController {

    private final ChatModel chatModel;

    public OllamaController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping
    public String getMethodName() {
        // ChatResponse response = chatModel.call(
        //         new Prompt(
        //                 "Generate the names of 5 famous pirates.",
        //                 OllamaChatOptions.builder()
        //                         .model(OllamaModel.LLAMA3_1)
        //                         .temperature(0.4)
        //                         .build()));
        return "chat";
    }

}

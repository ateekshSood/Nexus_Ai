package com.ateeksh.nexus_ai_package.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiTalk {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    public String getAiResponse(String prompt) {
        try {
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            return "Some error has occured we are cooked ensure that ollama is running";
        }
    }

}

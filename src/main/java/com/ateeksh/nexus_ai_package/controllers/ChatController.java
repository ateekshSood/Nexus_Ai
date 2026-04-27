package com.ateeksh.nexus_ai_package.controllers;

import com.ateeksh.nexus_ai_package.dto.ChatRequest;
import com.ateeksh.nexus_ai_package.model.ChatSession;
import com.ateeksh.nexus_ai_package.repository.ChatSessionRepository;
import com.ateeksh.nexus_ai_package.service.AiTalk;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class ChatController{

    private final AiTalk aitalk;
    private final ChatSessionRepository chatSessionRepository; 

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request){
        
        String chatId = request.getChatId();

        if (chatId == null || chatId.isBlank()){
            ChatSession newSession = new ChatSession();
            String title = request.getMessage().length() > 40
                ? request.getMessage().substring(0 , 40) + "..."
                : request.getMessage();
            newSession.setTitle(title);
            chatSessionRepository.save(newSession);
            chatId = newSession.getId(); 
        }

        return aitalk.getAiResponse(request.getMessage() , chatId);

    }
    
}
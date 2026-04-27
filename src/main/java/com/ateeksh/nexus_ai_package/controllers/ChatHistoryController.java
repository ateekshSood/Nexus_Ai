package com.ateeksh.nexus_ai_package.controllers;

import com.ateeksh.nexus_ai_package.model.ChatMessage;
import com.ateeksh.nexus_ai_package.model.ChatSession;
import com.ateeksh.nexus_ai_package.repository.ChatMessageRepository;
import com.ateeksh.nexus_ai_package.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @GetMapping
    public List<ChatSession> getAllSessions() {
        return chatSessionRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{sessionId}/messages")
    public List<ChatMessage> getMessagesForSession(@PathVariable String sessionId) {
        return chatMessageRepository.findBySession_IdOrderByTimestampAsc(sessionId);
    }

    @DeleteMapping("/{sessionId}")
    @Transactional
    public void deleteSession(@PathVariable String sessionId) {
        chatMessageRepository.deleteBySession_Id(sessionId);
        chatSessionRepository.deleteById(sessionId);
    }
}
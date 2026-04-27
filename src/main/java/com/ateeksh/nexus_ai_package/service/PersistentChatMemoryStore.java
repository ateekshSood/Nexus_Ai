package com.ateeksh.nexus_ai_package.service;

import com.ateeksh.nexus_ai_package.model.ChatMessage;
import com.ateeksh.nexus_ai_package.model.ChatSession;
import com.ateeksh.nexus_ai_package.repository.ChatMessageRepository;
import com.ateeksh.nexus_ai_package.repository.ChatSessionRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class PersistentChatMemoryStore implements ChatMemoryStore{

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;

    @Override
    public List<dev.langchain4j.data.message.ChatMessage> getMessages(Object memoryId){

        String sessionId = memoryId.toString();
        log.debug("Loading messages from db for session : {} " , sessionId);

        return chatMessageRepository.findBySession_IdOrderByTimestampAsc(sessionId)
            .stream()
            .map(this::toLC4jMessage)
            .collect(Collectors.toList());

    }

    @Override
    public void updateMessages(Object memoryId , List<dev.langchain4j.data.message.ChatMessage> messages){

        String sessionId = memoryId.toString();
        log.debug("saving {} messages to db for session: {}" , messages.size() , sessionId);

        ChatSession session = chatSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("chat session not found " + sessionId));

        chatMessageRepository.deleteBySession_Id(sessionId);

        messages.forEach(msg -> {
            ChatMessage dbMsg = new ChatMessage();
            dbMsg.setSession(session);
            if (msg instanceof UserMessage) {
                dbMsg.setSender("USER");
                dbMsg.setText(((UserMessage) msg).singleText());
            } else if (msg instanceof AiMessage) {
                dbMsg.setSender("AI");
                dbMsg.setText(((AiMessage) msg).text());
            }
            chatMessageRepository.save(dbMsg);
        });

    }


    @Override
    public void deleteMessages(Object memoryId) {
        chatMessageRepository.deleteBySession_Id(memoryId.toString());
    }


    private dev.langchain4j.data.message.ChatMessage toLC4jMessage(ChatMessage dbMsg) {
        if ("USER".equals(dbMsg.getSender())) {
            return UserMessage.from(dbMsg.getText());
        } else {
            return AiMessage.from(dbMsg.getText());
        }
    }


}
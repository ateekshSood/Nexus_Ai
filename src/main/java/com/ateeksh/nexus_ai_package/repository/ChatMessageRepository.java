package com.ateeksh.nexus_ai_package.repository;

import com.ateeksh.nexus_ai_package.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String>{
    
    List<ChatMessage> findBySession_IdOrderByTimestampAsc(String sessionId);

    void deleteBySession_Id(String sessionId);
}
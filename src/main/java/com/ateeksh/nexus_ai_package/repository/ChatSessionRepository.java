package com.ateeksh.nexus_ai_package.repository;

import com.ateeksh.nexus_ai_package.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String>{

    List<ChatSession> findAllByOrderByCreatedAtDesc();
}
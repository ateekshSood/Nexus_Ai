package com.ateeksh.nexus_ai_package.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data 
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage{

    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id" , nullable = false)
    private ChatSession session;

    private String sender; 

    @Column(columnDefinition = "TEXT")
    private String text;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist(){
        this.timestamp = LocalDateTime.now();
    }

    
}
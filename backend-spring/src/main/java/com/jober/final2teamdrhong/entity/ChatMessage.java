package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "is_deleted = false")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "message_content", columnDefinition = "TEXT",  nullable = false)
    private String messageContent;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;



    // ===== 관계 필드 =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;



    @Builder
    public ChatMessage(String messageContent, ChatSession chatSession) {
        this.messageContent = messageContent;
        this.chatSession = chatSession;
    }
}

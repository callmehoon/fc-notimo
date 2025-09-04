package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE chat_session SET is_deleted = true, deleted_at = NOW() WHERE session_id = ?")
@Where(clause = "is_deleted = false")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Integer sessionId;

    @Column(name = "session_title", nullable = false)
    private String sessionTitle;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;



    // ===== 관계 필드 =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;



    @Builder
    public ChatSession(String sessionTitle, Workspace workspace) {
        this.sessionTitle = sessionTitle;
        this.workspace = workspace;
    }
}

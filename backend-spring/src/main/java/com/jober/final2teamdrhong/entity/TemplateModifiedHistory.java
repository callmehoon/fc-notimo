package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "template_modified_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE template_modified_history SET is_deleted = true, deleted_at = NOW() WHERE history_id = ?")
@Where(clause = "is_deleted = false")
public class TemplateModifiedHistory {
    public enum Status {
        DRAFT, PENDING, APPROVED, REJECTED
    }



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @Column(name = "history_title")
    private String historyTitle;

    @Column(name = "history_content", columnDefinition = "TEXT")
    private String historyContent;

    @Column(name = "button_title", length = 50)
    private String buttonTitle;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;



    // ===== 관계 필드 =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_template_id", nullable = false)
    private IndividualTemplate individualTemplate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;


    @Builder
    public TemplateModifiedHistory(String historyTitle, String historyContent, String buttonTitle,
                                   Status status, IndividualTemplate individualTemplate, ChatMessage chatMessage) {
        this.historyTitle = historyTitle;
        this.historyContent = historyContent;
        this.buttonTitle = buttonTitle;
        this.status = status;
        this.individualTemplate = individualTemplate;
        this.chatMessage = chatMessage;
    }
}

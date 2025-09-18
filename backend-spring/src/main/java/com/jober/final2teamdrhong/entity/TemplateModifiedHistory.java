package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "template_modified_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("is_deleted = false")
public class TemplateModifiedHistory extends BaseEntity {

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

    // ===== 관계 필드 =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_template_id", nullable = false)
    private IndividualTemplate individualTemplate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;
}

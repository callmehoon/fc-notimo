package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "individual_template")

public class IndividualTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "individual_template_id")
    private Integer individualTemplateId;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspaceId;

    @Column(name = "individual_template_title")
    private String individualTemplateTitle;

    @Column(name = "individual_template_content", columnDefinition = "TEXT")
    private String individualTemplateContent;

    @Column(name = "button_title", length = 50)
    private String buttonTitle;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    @OneToMany
    @JoinColumn(name = "history_id")
    private TemplateModifiedHistory historyId;

    @OneToMany
    @JoinColumn(name = "favorite_id")
    private Favorite favoriteId;
}

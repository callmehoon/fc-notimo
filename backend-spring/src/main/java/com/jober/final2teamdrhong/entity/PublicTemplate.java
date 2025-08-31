package com.jober.final2teamdrhong.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@SQLDelete(sql = "UPDATE public_template SET is_deleted = true, deleted_at = NOW() WHERE public_template_id = ?")
public class PublicTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer publicTemplateId;

    @Column(nullable = false, length = 255)
    private String publicTemplateTitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicTemplateContent;

    // @Enumerated(EnumType.STRING)
    // @Column(name = "public_template_type", nullable = false)
    // private TemplateType type;

    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false)
    // private ButtonType buttonType;

    private Integer shareCount = 0;

    private Integer viewCount = 0;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    protected PublicTemplate() {}

    public PublicTemplate(String title, String content, Boolean isDeleted) {
        publicTemplateTitle = title;
        publicTemplateContent = content;
        this.isDeleted = isDeleted;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

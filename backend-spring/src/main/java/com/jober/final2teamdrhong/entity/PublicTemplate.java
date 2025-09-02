package com.jober.final2teamdrhong.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
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

    @Column(length = 50)
    private String buttonTitle;

    @Column(nullable = false)
    private Integer shareCount = 0;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(columnDefinition = "DATETIME", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    protected PublicTemplate() {}

    public PublicTemplate(String title, String content, Boolean isDeleted) {
        publicTemplateTitle = title;
        publicTemplateContent = content;
        this.isDeleted = isDeleted;
    }
}

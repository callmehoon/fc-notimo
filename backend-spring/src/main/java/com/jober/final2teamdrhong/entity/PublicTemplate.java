package com.jober.final2teamdrhong.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private Integer shareCount;

    @Column(nullable = false)
    private Integer viewCount;

    @Column(nullable = false)
    private Boolean isDeleted;

    @CreationTimestamp
    @Column(columnDefinition = "DATETIME", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    /**
     * 공개 템플릿 생성자
     * 
     * @param publicTemplateTitle 템플릿 제목
     * @param publicTemplateContent 템플릿 내용
     * @param buttonTitle 버튼 제목 (선택사항)
     */
    @Builder
    public PublicTemplate(String publicTemplateTitle, String publicTemplateContent, String buttonTitle) {
        this.publicTemplateTitle = publicTemplateTitle;
        this.publicTemplateContent = publicTemplateContent;
        this.buttonTitle = buttonTitle;
        this.viewCount = 0;
        this.shareCount = 0;
        this.isDeleted = false;
    }
    
    /**
     * 조회수를 1 증가시킨다.
     * 템플릿이 조회될 때마다 호출되어야 한다.
     */
    public void increaseViewCount() {
        this.viewCount++;
    }
    
    /**
     * 공유수를 1 증가시킨다.
     * 템플릿이 '가져오기'될 때마다 호출되어야 한다.
     */
    public void increaseShareCount() {
        this.shareCount++;
    }
}

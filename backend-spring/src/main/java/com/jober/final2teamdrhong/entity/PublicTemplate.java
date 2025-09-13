package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity(name = "public_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("is_deleted = false")
public class PublicTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "public_template_id")
    private Integer publicTemplateId;

    @Column(name = "public_template_title", nullable = false)
    private String publicTemplateTitle;

    @Column(name = "public_template_content", nullable = false, columnDefinition = "TEXT")
    private String publicTemplateContent;

    @Column(name = "button_title", length = 50)
    private String buttonTitle;

    @Builder.Default
    @Column(name = "share_count", nullable = false)
    private Integer shareCount = 0;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

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

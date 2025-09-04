package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "individual_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 생성
@AllArgsConstructor // 모든 필드를 매개 변수로 받는 생성자를 자동 생성
@Builder

public class IndividualTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "individual_template_id")
    private Integer individualTemplateId;

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

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspaceId;

    @OneToMany(mappedBy = "individualTemplate")
    private List<TemplateModifiedHistory> histories;

    @OneToMany(mappedBy = "individualTemplate")
    private List<Favorite> favorites;
}


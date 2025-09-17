package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Table(name = "individual_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 생성
@SuperBuilder
@SQLRestriction("is_deleted = false")
public class IndividualTemplate extends BaseEntity {

    public enum Status {
        DRAFT, PENDING, APPROVED, REJECTED
    }

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.DRAFT;


    // 빌더로 null이 들어오거나, 다른 경로로 null인 경우에도 DB에 들어가기 전 DRAFT로 보정.
    @PrePersist
    void applyDefaultStatus() {
        if (status == null) status = Status.DRAFT;
    }

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @OneToMany(mappedBy = "individualTemplate")
    private List<TemplateModifiedHistory> histories;

    @OneToOne(mappedBy = "individualTemplate")
    private Favorite favorite;
}
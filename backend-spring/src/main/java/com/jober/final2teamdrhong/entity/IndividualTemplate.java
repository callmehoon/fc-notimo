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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @PrePersist
    public void applyDefaultStatus() {
        if (status == null){
            status = Status.DRAFT;
        }
    }

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @OneToMany(mappedBy = "individualTemplate")
    private List<TemplateModifiedHistory> histories;

    // 엔티티의 상태 변화 즉, DB 값의 변화이기 때문에 엔티티에서 작성
    public void update(String individualTemplateTitle,
                       String individualTemplateContent,
                       String buttonTitle,
                       Status status) {
        this.individualTemplateTitle = individualTemplateTitle;
        this.individualTemplateContent = individualTemplateContent;
        this.buttonTitle = buttonTitle;
        this.status = status; // 항상 DRAFT
    }
}
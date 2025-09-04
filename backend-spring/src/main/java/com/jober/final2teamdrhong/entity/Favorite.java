package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "favorite", uniqueConstraints = {
        // 스페이스와 템플릿들의 조합을 UNIQUE로 설정. 데이터 중복 방지
        @UniqueConstraint(columnNames = {"workspace_id", "public_template_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Integer favoriteId;



    // ===== 관계 필드 =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_template_id")
    private PublicTemplate publicTemplate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_template_id")
    private IndividualTemplate individualTemplate;



    @Builder
    public Favorite(Workspace workspace, PublicTemplate publicTemplate, IndividualTemplate individualTemplate) {
        this.workspace = workspace;
        this.publicTemplate = publicTemplate;
        this.individualTemplate = individualTemplate;
    }
}

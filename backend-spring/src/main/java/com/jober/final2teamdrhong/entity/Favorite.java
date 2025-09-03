package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "favorite", uniqueConstraints = {
        // 스페이스와 템플릿들의 조합을 UNIQUE로 설정. 데이터 중복 방지
        @UniqueConstraint(columnNames = {"workspace_id", "public_template_id"}),
        @UniqueConstraint(columnNames = {"workspace_id", "individual_template_id"})
})
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Integer favoriteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_template_id")
    private PublicTemplate publicTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_template_id")
    private IndividualTemplate individualTemplate;
}

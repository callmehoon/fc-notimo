package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.IndividualTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndividualTemplateRepository extends JpaRepository<IndividualTemplate, Integer> {
    default IndividualTemplate findByIdOrThrow(Integer templateId) {
        return findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("해당 개인 템플릿을 찾을 수 없습니다."));
    }

    // 개인 템플릿 목록 전체 조회
    Page<IndividualTemplate> findByWorkspace_WorkspaceId(Integer workspaceId, Pageable pageable);

    // 개인 템플릿 목록 상태별 조회
    Page<IndividualTemplate> findByWorkspace_WorkspaceIdAndStatus(
            Integer workspaceId,
            IndividualTemplate.Status status,
            Pageable pageable
    );

    // 개인 템플릿 단일 조회
    Optional<IndividualTemplate> findByIndividualTemplateIdAndWorkspace_WorkspaceId(Integer individualTemplateId, Integer workspaceId);

    // 제목별 전체 조회 (가나다순)
    @Query("SELECT i FROM IndividualTemplate i " +
            "WHERE i.workspace.workspaceId = :workspaceId AND i.isDeleted = false " +
            "ORDER BY CASE WHEN i.individualTemplateTitle IS NULL THEN 1 ELSE 0 END, " +
            "         i.individualTemplateTitle ASC")
    Page<IndividualTemplate> findAllByWorkspaceOrderByTitleAsc(
            @Param("workspaceId") Integer workspaceId,
            Pageable pageable);

    // 제목 + 상태 조회 (가나다순)
    @Query("SELECT i FROM IndividualTemplate i " +
            "WHERE i.workspace.workspaceId = :workspaceId " +
            "AND i.status = :status AND i.isDeleted = false " +
            "ORDER BY CASE WHEN i.individualTemplateTitle IS NULL THEN 1 ELSE 0 END, " +
            "         i.individualTemplateTitle ASC")
    Page<IndividualTemplate> findAllByWorkspaceAndStatusOrderByTitleAsc(
            @Param("workspaceId") Integer workspaceId,
            @Param("status") IndividualTemplate.Status status,
            Pageable pageable);
}

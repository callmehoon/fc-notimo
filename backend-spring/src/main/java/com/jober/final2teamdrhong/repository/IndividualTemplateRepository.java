package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.IndividualTemplate;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndividualTemplateRepository extends JpaRepository<IndividualTemplate, Integer> {

    // 개인 템플릿 목록 전체 조회
    Page<IndividualTemplate> findByWorkspace_WorkspaceIdAndIsDeletedFalse(Integer workspaceId, Pageable pageable);

    // 개인 템플릿 목록 상태별 조회
    Page<IndividualTemplate> findByWorkspace_WorkspaceIdAndIsDeletedFalseAndStatus(
            Integer workspaceId,
            IndividualTemplate.Status status,
            Pageable pageable
    );

    // 개인 템플릿 단일 조회
    Optional<IndividualTemplate> findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(Integer individualTemplateId, Integer workspaceId);

    @Modifying
    @Query("""
    UPDATE IndividualTemplate t
       SET t.isDeleted = true
     WHERE t.individualTemplateId = :individualTemplateId
       AND t.isDeleted = false
    """)
    int softDeleteByIndividualTemplateId(@Param("individualTemplateId") Integer individualTemplateId);

}

package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.TemplateModifiedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateModifiedHistoryRepository extends JpaRepository<TemplateModifiedHistory, Integer> {
    /**
     * 특정 개인 템플릿에 속한 모든 수정 이력을 생성일자 최신순으로 조회합니다.
     * @param individualTemplate 조회할 개인 템플릿 엔티티
     * @return TemplateModifiedHistory 엔티티 리스트
     */
    List<TemplateModifiedHistory> findAllByIndividualTemplateOrderByCreatedAtDesc(IndividualTemplate individualTemplate);
}

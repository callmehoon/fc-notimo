package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.IndividualTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndividualTemplateRepository extends JpaRepository<IndividualTemplate, Integer> {
    default IndividualTemplate findByIdOrThrow(Integer templateId) {
        return findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("해당 개인 템플릿을 찾을 수 없습니다."));
    }
}

package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.PublicTemplate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicTemplateRepository
    extends JpaRepository<PublicTemplate, Integer>,
            JpaSpecificationExecutor<PublicTemplate> {
    default PublicTemplate findByIdOrThrow(Integer templateId) {
        return findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공용 템플릿을 찾을 수 없습니다."));
    }
}

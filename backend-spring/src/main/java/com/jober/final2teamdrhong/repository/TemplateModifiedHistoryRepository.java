package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.TemplateModifiedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateModifiedHistoryRepository extends JpaRepository<TemplateModifiedHistory, Integer> {}

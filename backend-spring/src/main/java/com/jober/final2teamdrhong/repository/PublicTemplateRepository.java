package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.PublicTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicTemplateRepository extends JpaRepository<PublicTemplate, Integer> {

}

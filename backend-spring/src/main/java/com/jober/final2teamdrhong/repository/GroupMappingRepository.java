package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.GroupMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMappingRepository extends JpaRepository<GroupMapping, Integer> {

}

package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> {

    default Workspace findByIdOrThrow(Integer workspaceId) {
        return findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스를 찾을 수 없습니다."));
    }
}

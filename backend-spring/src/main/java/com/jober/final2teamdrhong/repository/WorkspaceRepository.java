package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> {

    /**
     * 주어진 URL을 가진 워크스페이스가 존재하는지 확인합니다.
     * @param workspaceUrl 확인할 고유 URL
     * @return 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByWorkspaceUrl(String workspaceUrl);
}

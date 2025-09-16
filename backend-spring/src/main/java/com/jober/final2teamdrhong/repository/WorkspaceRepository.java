package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> {

    /**
     * 주어진 URL을 가진 워크스페이스가 존재하는지 확인합니다.
     *
     * @param workspaceUrl 확인할 고유 URL
     * @return 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByWorkspaceUrl(String workspaceUrl);

    /**
     * 특정 사용자 ID에 속한 모든 워크스페이스 목록을 조회합니다.
     *
     * @param userId 워크스페이스를 조회할 사용자의 ID
     * @return 해당 사용자의 모든 워크스페이스 엔티티 리스트. 결과가 없으면 빈 리스트를 반환합니다.
     */
    List<Workspace> findAllByUser_UserId(Integer userId);

    /**
     * 특정 사용자의 워크스페이스 상세 정보를 조회합니다.
     * <p>
     * 워크스페이스 ID와 사용자 ID를 모두 사용하여 해당 사용자가 소유한 워크스페이스만 조회하므로,
     * 다른 사용자의 워크스페이스에 접근하는 것을 방지합니다.
     *
     * @param workspaceId 조회할 워크스페이스의 ID
     * @param userId      워크스페이스의 소유자 ID
     * @return 조건에 맞는 워크스페이스 엔티티를 Optional로 감싸서 반환합니다. 소유권이 없거나 존재하지 않으면 빈 Optional을 반환합니다.
     */
    Optional<Workspace> findByWorkspaceIdAndUser_UserId(Integer workspaceId, Integer userId);

    boolean existsByWorkspaceIdAndUser_UserId(Integer workspaceId, Integer userId);
}

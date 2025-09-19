package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.Recipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Integer> {

    /**
     * 특정 워크스페이스 ID에 해당하는 모든 수신자 목록을 조회합니다.
     *
     * @param workspaceId 수신자를 조회할 워크스페이스의 ID
     * @param pageable 클라이언트가 요청한 페이지 정보 (페이지 번호, 사이즈, 정렬)
     * @return 페이징 처리된 수신자 엔티티 정보 (데이터 목록 + 전체 페이지 수 등)
     */
    Page<Recipient> findAllByWorkspace_WorkspaceId(Integer workspaceId, Pageable pageable);
}

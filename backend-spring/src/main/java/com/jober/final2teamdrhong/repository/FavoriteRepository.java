package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.Favorite;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    /**
     * 특정 워크스페이스와 개인 템플릿으로 즐겨찾기 정보를 조회합니다.
     * @param workspace 조회할 워크스페이스 엔티티
     * @param individualTemplate 조회할 개인 템플릿 엔티티
     * @return 존재하면 Optional<Favorite>, 존재하지 않으면 Optional.empty()
     */
    Optional<Favorite> findByWorkspaceAndIndividualTemplate(Workspace workspace, IndividualTemplate individualTemplate);

    /**
     * 특정 워크스페이스와 공용 템플릿으로 즐겨찾기 정보를 조회합니다.
     * @param workspace 조회할 워크스페이스 엔티티
     * @param publicTemplate 조회할 공용 템플릿 엔티티
     * @return 존재하면 Optional<Favorite>, 존재하지 않으면 Optional.empty()
     */
    Optional<Favorite> findByWorkspaceAndPublicTemplate(Workspace workspace, PublicTemplate publicTemplate);

    /**
     * 특정 워크스페이스에 속한 모든 즐겨찾기 목록을 조회합니다.
     * @param workspace 조회의 기준이 되는 워크스페이스 엔티티
     * @return 해당 워크스페이스의 Favorite 엔티티 리스트
     */
    List<Favorite> findAllByWorkspace(Workspace workspace);

}

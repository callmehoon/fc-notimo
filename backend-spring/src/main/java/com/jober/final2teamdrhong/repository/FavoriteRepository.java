package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.Favorite;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}

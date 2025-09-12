package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.Favorite;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * 특정 워크스페이스에 속한 모든 즐겨찾기 목록을 페이징 없이 최신순으로 조회합니다.
     * @param workspace 조회의 기준이 되는 워크스페이스
     * @return 즐겨찾기 목록
     */
    List<Favorite> findAllByWorkspaceOrderByFavoriteIdDesc(Workspace workspace);

    /**
     * 특정 워크스페이스에 속한 공용 템플릿 즐겨찾기 목록을 페이징하여 조회합니다.
     * @param workspace 조회의 기준이 되는 워크스페이스
     * @param pageable 페이징 정보 (정렬 포함)
     * @return 페이징된 즐겨찾기 목록
     */
    Page<Favorite> findByWorkspaceAndPublicTemplateIsNotNull(Workspace workspace, Pageable pageable);

    /**
     * 특정 워크스페이스에 속한 개인 템플릿 즐겨찾기 목록을 페이징하여 조회합니다.
     * @param workspace 조회의 기준이 되는 워크스페이스
     * @param pageable 페이징 정보 (정렬 포함)
     * @return 페이징된 즐겨찾기 목록
     */
    Page<Favorite> findByWorkspaceAndIndividualTemplateIsNotNull(Workspace workspace, Pageable pageable);
}

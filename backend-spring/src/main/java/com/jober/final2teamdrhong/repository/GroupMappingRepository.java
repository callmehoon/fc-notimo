package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.GroupMapping;
import com.jober.final2teamdrhong.entity.PhoneBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupMappingRepository extends JpaRepository<GroupMapping, Integer> {

    /**
     * 특정 주소록에 이미 매핑된 모든 수신자들의 ID 목록을 조회합니다.
     * <p>
     * 이 메소드는 주소록에 새로운 수신자를 추가하기 전,
     * 중복 추가를 방지하기 위한 목적으로 사용됩니다.
     *
     * @param phoneBook 조회할 주소록 엔티티
     * @return 해당 주소록에 속한 수신자들의 ID 리스트
     */
    @Query("SELECT gm.recipient.recipientId FROM GroupMapping gm WHERE gm.phoneBook = :phoneBook")
    List<Integer> findRecipientIdsByPhoneBook(@Param("phoneBook") PhoneBook phoneBook);

    /**
     * 특정 주소록에 속한 GroupMapping 목록을 페이징하여 조회합니다.
     * <p>
     * 수신자의 생성 시간(createdAt) 내림차순으로 정렬되어 반환됩니다.
     * 이를 통해 최신에 생성된 수신자가 먼저 표시됩니다.
     *
     * @param phoneBook 조회할 주소록 엔티티
     * @param pageable  페이징 및 정렬 정보 (정렬 조건은 무시되고 수신자 생성 시간 기준으로 정렬됨)
     * @return 페이징된 GroupMapping 엔티티 목록 (Page<GroupMapping>)
     */
    @Query("SELECT gm FROM GroupMapping gm WHERE gm.phoneBook = :phoneBook ORDER BY gm.recipient.createdAt DESC")
    Page<GroupMapping> findByPhoneBook(@Param("phoneBook") PhoneBook phoneBook, Pageable pageable);

    /**
     * 전달된 GroupMapping 엔티티 목록을 Bulk Update를 통해 일괄 소프트 딜리트 처리합니다.
     * isDeleted 플래그, updatedAt, deletedAt을 모두 파라미터로 전달받은 시간으로 명시적으로 업데이트합니다.
     * <p>
     * JPQL을 사용하여 단일 UPDATE 쿼리를 실행하므로 성능이 우수합니다.
     * 각 엔티티를 개별적으로 수정하는 방식과 달리, N+1 문제를 방지할 수 있습니다.
     * <p>
     * 소프트 삭제 시 다음 필드들이 업데이트됩니다:
     * <ul>
     *     <li>isDeleted: true로 설정하여 삭제 상태 표시</li>
     *     <li>updatedAt: 현재 시간으로 설정하여 수정 시점 기록</li>
     *     <li>deletedAt: 현재 시간으로 설정하여 삭제 시점 기록</li>
     * </ul>
     * <p>
     * 실행 후 영속성 컨텍스트는 자동으로 클리어되므로, 이후 조회 시 DB의 최신 상태가 반영됩니다.
     *
     * @param mappings 소프트 딜리트할 GroupMapping 엔티티 목록
     * @param now      삭제 시점으로 기록할 현재 시간 (LocalDateTime)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GroupMapping gm SET gm.isDeleted = true, gm.updatedAt = :now, gm.deletedAt = :now WHERE gm IN :mappings")
    void softDeleteAllInBatch(@Param("mappings") List<GroupMapping> mappings, @Param("now") LocalDateTime now);

    /**
     * ID 목록으로 GroupMapping 조회 (소프트 삭제된 엔티티 포함)
     * <p>
     * 이 메서드는 {@link GroupMapping} 엔티티에 적용된
     * {@code @SQLRestriction("is_deleted = false")} 제약 조건을 우회하기 위해 네이티브 SQL 쿼리를 사용합니다.
     * 따라서 소프트 딜리트 처리된 매핑을 포함하여 모든 상태의 매핑을 조회할 수 있습니다.
     * <p>
     * 삭제 작업 후 정확한 DB 시간 정보가 담긴 DTO 반환을 위해 사용됩니다.
     *
     * @param mappingIds 조회할 GroupMapping ID 목록
     * @return 소프트 삭제된 것을 포함한 GroupMapping 목록
     */
    @Query(value = "SELECT * FROM group_mapping WHERE group_mapping_id IN :mappingIds", nativeQuery = true)
    List<GroupMapping> findAllByIdIncludingDeleted(@Param("mappingIds") List<Integer> mappingIds);

    /**
     * 특정 주소록과 수신자 ID 목록에 해당하는 GroupMapping 엔티티들을 조회합니다.
     * <p>
     * 이 메소드는 주소록에서 특정 수신자들을 삭제하기 전,
     * 실제로 삭제 가능한 매핑 관계를 찾기 위한 목적으로 사용됩니다.
     * 요청된 수신자 ID 중 해당 주소록에 실제로 존재하는 매핑만 반환됩니다.
     *
     * @param phoneBook   대상 주소록 엔티티
     * @param recipientIds 조회할 수신자 ID 목록
     * @return 조건에 일치하는 GroupMapping 엔티티 목록
     */
    List<GroupMapping> findAllByPhoneBookAndRecipient_RecipientIdIn(PhoneBook phoneBook, List<Integer> recipientIds);
}

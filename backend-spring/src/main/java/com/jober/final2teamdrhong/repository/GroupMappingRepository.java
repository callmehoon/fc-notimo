package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.GroupMapping;
import com.jober.final2teamdrhong.entity.PhoneBook;
import com.jober.final2teamdrhong.entity.Recipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

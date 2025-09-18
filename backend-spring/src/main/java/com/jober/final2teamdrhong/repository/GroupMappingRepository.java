package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.GroupMapping;
import com.jober.final2teamdrhong.entity.PhoneBook;
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
}

package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.PhoneBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhoneBookRepository extends JpaRepository<PhoneBook, Integer> {

    /**
     * 주소록 ID와 워크스페이스 ID를 사용하여 특정 워크스페이스에 속한 주소록을 조회합니다.
     * <p>
     * 이 메소드는 주소록에 수신자 추가, 삭제 등 특정 수신자에 대한 작업을 수행하기 전,
     * 해당 주소록이 올바른 워크스페이스에 속해 있는지 검증하는 데 사용될 수 있습니다.
     *
     * @param phoneBookId 조회할 주소록의 고유 ID
     * @param workspaceId 주소록이 속한 워크스페이스의 ID
     * @return 주소록 엔티티를 담은 Optional 객체. 해당하는 주소록이 없으면 Optional.empty()를 반환합니다.
     */
    Optional<PhoneBook> findByPhoneBookIdAndWorkspace_WorkspaceId(Integer phoneBookId, Integer workspaceId);

    /**
     * 특정 워크스페이스 ID에 속한 모든 주소록 목록을 조회합니다.
     *
     * @param workspaceId 주소록을 조회할 워크스페이스의 ID
     * @return 해당 워크스페이스의 모든 주소록 엔티티 리스트. 결과가 없으면 빈 리스트를 반환합니다.
     */
    List<PhoneBook> findAllByWorkspace_WorkspaceId(Integer workspaceId);

    /**
     * ID를 기준으로 주소록(PhoneBook) 엔티티를 조회합니다.
     * <p>
     * 이 메서드는 {@link PhoneBook} 엔티티에 적용된
     * {@code @SQLRestriction("is_deleted = false")} 제약 조건을 우회하기 위해 네이티브 SQL 쿼리를 사용합니다.
     * 따라서 소프트 딜리트 처리된 주소록을 포함하여 모든 상태의 주소록을 조회할 수 있습니다.
     * <p>
     * 주로 소프트 딜리트 트랜잭션 내에서 DB에 반영된 최종 상태(예: {@code deletedAt} 타임스탬프)를 정확히 다시 읽어와야 할 때 사용됩니다.
     *
     * @param phoneBookId 조회할 주소록의 ID
     * @return 조회된 PhoneBook 엔티티를 담은 Optional 객체. ID가 존재하지 않으면 빈 Optional을 반환합니다.
     */
    @Query(value = "SELECT * FROM phone_book WHERE phone_book_id = :phoneBookId", nativeQuery = true)
    Optional<PhoneBook> findByIdIncludingDeleted(@Param("phoneBookId") Integer phoneBookId);
}

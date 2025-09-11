package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.phonebook.PhoneBookRequest;
import com.jober.final2teamdrhong.dto.phonebook.PhoneBookResponse;
import com.jober.final2teamdrhong.entity.PhoneBook;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.PhoneBookRepository;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주소록(PhoneBook) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhoneBookService {

    private final PhoneBookRepository phoneBookRepository;
    private final WorkspaceRepository workspaceRepository;

    /**
     * 특정 워크스페이스에 새로운 주소록을 생성합니다.
     *
     * @param createDTO   주소록 생성을 위한 요청 데이터
     * @param workspaceId 주소록을 추가할 워크스페이스의 ID
     * @param userId      요청을 보낸 사용자의 ID (인가에 사용)
     * @return 생성된 주소록의 정보({@link PhoneBookResponse.SimpleDTO})
     * @throws IllegalArgumentException 해당 워크스페이스가 존재하지 않거나, 사용자가 접근 권한이 없을 경우 발생
     */
    @Transactional
    public PhoneBookResponse.SimpleDTO createPhoneBook(PhoneBookRequest.CreateDTO createDTO, Integer workspaceId, Integer userId) {
        // 1. 인가(Authorization): 요청한 사용자가 워크스페이스에 접근 권한이 있는지 확인합니다.
        Workspace workspace = workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + workspaceId));

        // 2. 엔티티 생성: DTO의 데이터를 기반으로 PhoneBook 엔티티를 생성합니다.
        PhoneBook phoneBook = PhoneBook.builder()
                .phoneBookName(createDTO.getPhoneBookName())
                .phoneBookMemo(createDTO.getPhoneBookMemo())
                .workspace(workspace)
                .build();

        // 3. 엔티티 저장 및 DTO 변환 후 반환
        PhoneBook createdPhoneBook = phoneBookRepository.save(phoneBook);

        return new PhoneBookResponse.SimpleDTO(createdPhoneBook);
    }
}

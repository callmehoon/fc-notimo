package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.recipient.RecipientRequest;
import com.jober.final2teamdrhong.dto.recipient.RecipientResponse;
import com.jober.final2teamdrhong.entity.Recipient;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.RecipientRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 수신자(Recipient) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final WorkspaceRepository workspaceRepository;

    /**
     * 특정 워크스페이스에 새로운 수신자를 생성합니다.
     *
     * @param createDTO   수신자 생성을 위한 요청 데이터
     * @param workspaceId 수신자를 추가할 워크스페이스의 ID
     * @param userId      요청을 보낸 사용자의 ID (인가에 사용)
     * @return 생성된 수신자의 정보({@link RecipientResponse.SimpleDTO})
     * @throws IllegalArgumentException 해당 워크스페이스가 존재하지 않거나, 사용자가 접근 권한이 없을 경우 발생
     */
    @Transactional
    public RecipientResponse.SimpleDTO createRecipient(RecipientRequest.CreateDTO createDTO, Integer workspaceId, Integer userId) {
        // 1. 인가(Authorization): 요청한 사용자가 워크스페이스에 접근 권한이 있는지 확인합니다.
        Workspace workspace = workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + workspaceId));

        // 2. 엔티티 생성: DTO의 데이터를 기반으로 Recipient 엔티티를 생성합니다.
        Recipient recipient = Recipient.builder()
                .recipientName(createDTO.getRecipientName())
                .recipientPhoneNumber(createDTO.getRecipientPhoneNumber())
                .recipientMemo(createDTO.getRecipientMemo())
                .workspace(workspace)
                .build();

        // 3. 엔티티 저장 및 DTO 변환 후 반환
        Recipient savedRecipient = recipientRepository.save(recipient);

        return new RecipientResponse.SimpleDTO(savedRecipient);
    }

    /**
     * 특정 워크스페이스에 속한 모든 수신자 목록을 페이징하여 조회합니다.
     * <p>
     * 이 메서드는 먼저 요청한 사용자가 해당 워크스페이스에 대한 접근 권한이 있는지 확인합니다.
     * 권한이 확인되면, 해당 워크스페이스의 모든 수신자 정보를 DTO 리스트로 변환하여 반환합니다.
     *
     * @param workspaceId 수신자 목록을 조회할 워크스페이스의 ID
     * @param userId      요청을 보낸 사용자의 ID (인가에 사용)
     * @param pageable 페이징 및 정렬 요청 정보
     * @return 페이징 처리된 수신자 정보가 담긴 Page<{@link RecipientResponse.SimpleDTO}> 객체
     * @throws IllegalArgumentException 해당 워크스페이스가 존재하지 않거나, 사용자가 접근 권한이 없을 경우 발생
     */
    public Page<RecipientResponse.SimpleDTO> readRecipients(Integer workspaceId, Integer userId, Pageable pageable) {
        workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + workspaceId));

        Page<Recipient> recipientPage = recipientRepository.findAllByWorkspace_WorkspaceId(workspaceId, pageable);

        return recipientPage.map(RecipientResponse.SimpleDTO::new);
    }
}

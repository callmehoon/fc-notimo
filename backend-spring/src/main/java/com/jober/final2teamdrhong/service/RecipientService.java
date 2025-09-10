package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.recipient.RecipientRequest;
import com.jober.final2teamdrhong.dto.recipient.RecipientResponse;
import com.jober.final2teamdrhong.entity.Recipient;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.RecipientRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
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
     * @throws IllegalArgumentException 해당 워크스페이스가 존재하지 않거나, 사용자가 접근 권한이 없을
    경우 발생
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
}

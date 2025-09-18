package com.jober.final2teamdrhong.service.validator;

import com.jober.final2teamdrhong.entity.Recipient;
import com.jober.final2teamdrhong.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecipientValidator {

    private final RecipientRepository recipientRepository;

    /**
     * 특정 워크스페이스에 해당 수신자가 존재하는지 검증합니다.
     * 검증에 성공하면 수신자 엔티티를 반환하고, 실패하면 예외를 발생시킵니다.
     *
     * @param workspaceId 수신자가 속한 워크스페이스의 ID
     * @param recipientId 검증할 수신자의 ID
     * @return 검증에 성공한 Recipient 엔티티
     * @throws IllegalArgumentException 해당 워크스페이스에 수신자가 존재하지 않을 경우
     */
    public Recipient validateAndGetRecipient(Integer workspaceId, Integer recipientId) {
        return recipientRepository.findByRecipientIdAndWorkspace_WorkspaceId(recipientId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스에 존재하지 않는 수신자입니다. ID: " + recipientId));
    }

    /**
     * 요청받은 수신자 ID 리스트가 모두 해당 워크스페이스에 존재하는지 검증합니다.
     * 검증에 성공하면 수신자 엔티티 리스트를 반환하고, 실패하면 예외를 발생시킵니다.
     *
     * @param workspaceId 수신자들이 속한 워크스페이스의 ID
     * @param recipientIds 검증할 수신자들의 ID 리스트
     * @return 검증에 성공한 Recipient 엔티티 리스트
     * @throws IllegalArgumentException ID 개수 불일치 또는 존재하지 않는 수신자가 포함된 경우
     */
    public List<Recipient> validateAndGetRecipients(Integer workspaceId, List<Integer> recipientIds) {
        List<Recipient> recipientList = recipientRepository.findAllByWorkspace_WorkspaceIdAndRecipientIdIn(workspaceId, recipientIds);
        if (recipientList.size() != recipientIds.size()) {
            throw new IllegalArgumentException("요청된 수신자 목록에 유효하지 않거나 권한이 없는 ID가 포함되어 있습니다.");
        }
        return recipientList;
    }
}

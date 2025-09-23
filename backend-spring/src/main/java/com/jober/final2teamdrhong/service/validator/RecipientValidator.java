package com.jober.final2teamdrhong.service.validator;

import com.jober.final2teamdrhong.entity.Recipient;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipientValidator {

    private final RecipientRepository recipientRepository;

    /**
     * 특정 워크스페이스에 동일한 이름과 전화번호를 가진 수신자가 이미 존재하는지 검증합니다.
     * 중복이 발견되면 예외를 발생시킵니다.
     *
     * @param workspace            검사를 수행할 워크스페이스 엔티티
     * @param recipientName        중복 여부를 확인할 수신자 이름
     * @param recipientPhoneNumber 중복 여부를 확인할 수신자 전화번호
     * @throws IllegalArgumentException 동일한 이름과 번호의 수신자가 이미 존재할 경우
     */
    public void validateNoDuplicateRecipientExists(Workspace workspace, String recipientName, String recipientPhoneNumber) {
        if (recipientRepository.existsByWorkspaceAndRecipientNameAndRecipientPhoneNumber(workspace, recipientName, recipientPhoneNumber)) {
            throw new IllegalArgumentException("해당 워크스페이스에 동일한 이름과 번호의 수신자가 이미 존재합니다.");
        }
    }

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
     * 수신자 정보 수정 시, 변경하려는 이름과 전화번호가 다른 수신자와 중복되는지 검증합니다.
     * 자기 자신은 중복 검사 대상에서 제외합니다.
     *
     * @param workspace            검사를 수행할 워크스페이스 엔티티
     * @param recipientName        중복 여부를 확인할 수신자 이름
     * @param recipientPhoneNumber 중복 여부를 확인할 수신자 전화번호
     * @param recipientId          현재 수정 중인 수신자의 ID (검사 대상에서 제외됨)
     * @throws IllegalArgumentException 변경하려는 정보가 다른 수신자와 중복될 경우
     */
    public void validateNoDuplicateRecipientExistsOnUpdate(Workspace workspace, String recipientName, String recipientPhoneNumber, Integer recipientId) {
        if (recipientRepository.existsByWorkspaceAndRecipientNameAndRecipientPhoneNumberAndRecipientIdNot(workspace, recipientName, recipientPhoneNumber, recipientId)) {
            throw new IllegalArgumentException("해당 정보와 동일한 다른 수신자가 이미 존재합니다.");
        }
    }
}

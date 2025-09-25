package com.jober.final2teamdrhong.service.validator;

import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceValidator {

    private final WorkspaceRepository workspaceRepository;
    private final IndividualTemplateRepository individualTemplateRepository;

    /**
     * 워크스페이스 '생성' 시 URL이 중복되는지 검증합니다.
     *
     * @param workspaceUrl 검증할 워크스페이스 URL
     * @throws IllegalArgumentException URL이 이미 존재할 경우
     */
    public void validateUrlOnCreate(String workspaceUrl) {
        if (workspaceRepository.existsByWorkspaceUrlAndIsDeletedFalse(workspaceUrl)) {
            throw new IllegalArgumentException("이미 사용 중인 URL입니다. 다른 URL을 입력해주세요.");
        }
    }

    /**
     * 워크스페이스의 존재 여부와 사용자의 접근 권한을 검증합니다.
     * 검증에 성공하면 워크스페이스 엔티티를 반환하고, 실패하면 예외를 발생시킵니다.
     *
     * @param workspaceId 검증할 워크스페이스의 ID
     * @param userId      접근을 시도하는 사용자의 ID
     * @return 검증에 성공한 Workspace 엔티티
     * @throws IllegalArgumentException 워크스페이스가 존재하지 않거나 사용자에게 접근 권한이 없을 경우
     */
    public Workspace validateAndGetWorkspace(Integer workspaceId, Integer userId) {
        return workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + workspaceId));
    }

    /**
     * 워크스페이스 '수정' 시 URL이 중복되는지 검증합니다.
     * <p>
     * URL이 변경되지 않은 경우 검증을 생략하며,
     * URL이 변경된 경우 현재 워크스페이스를 제외하고 다른 활성 워크스페이스 중에서만 중복을 검사합니다.
     * 삭제된 워크스페이스는 검사 대상에서 제외됩니다.
     *
     * @param existingWorkspace 수정 대상이 되는 기존 워크스페이스 엔티티
     * @param newWorkspaceUrl   새롭게 변경하려는 워크스페이스 URL
     * @throws IllegalArgumentException 변경하려는 URL이 다른 활성 워크스페이스에서 이미 사용 중일 경우
     */
    public void validateUrlOnUpdate(Workspace existingWorkspace, String newWorkspaceUrl) {
        // URL이 변경되지 않았다면 검증 생략
        if (existingWorkspace.getWorkspaceUrl().equals(newWorkspaceUrl)) {
            return;
        }

        // 현재 워크스페이스를 제외하고 중복 검사
        if (workspaceRepository.existsByWorkspaceUrlAndIsDeletedFalseAndWorkspaceIdNot(newWorkspaceUrl, existingWorkspace.getWorkspaceId())) {
            throw new IllegalArgumentException("이미 사용 중인 URL입니다. 다른 URL을 입력해주세요.");
        }
    }

    /**
     * 특정 개인 템플릿이 주어진 워크스페이스에 속해 있는지 검증합니다.
     *
     * @param workspaceId 검증의 기준이 되는 워크스페이스 ID
     * @param individualTemplateId 검증할 개인 템플릿 ID
     * @throws IllegalArgumentException 템플릿이 워크스페이스에 속해있지 않을 경우
     */
    public IndividualTemplate validateTemplateOwnership(Integer workspaceId, Integer individualTemplateId) {
        return individualTemplateRepository.findByIndividualTemplateIdAndWorkspace_WorkspaceId(individualTemplateId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스에 존재하지 않는 템플릿입니다."));
    }
}

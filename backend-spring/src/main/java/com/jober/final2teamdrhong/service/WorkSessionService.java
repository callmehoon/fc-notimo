package com.jober.final2teamdrhong.service;


import com.jober.final2teamdrhong.dto.worksession.WorkSessionRequest;
import com.jober.final2teamdrhong.dto.worksession.WorkSessionResponse;
import com.jober.final2teamdrhong.entity.ChatSession;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.ChatSessionRepository;
import com.jober.final2teamdrhong.service.validator.WorkspaceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final WorkspaceValidator workspaceValidator;

    /**
     * 새로운 채팅방을 생성합니다.
     *
     * @param request 채팅방 생성 요청 DTO (워크스페이스 ID, 채팅방 제목 포함)
     * @param userId 채팅방을 생성하는 사용자의 ID
     * @return 생성된 채팅방의 정보 (InfoDTO)
     * @throws IllegalArgumentException 워크스페이스가 존재하지 않거나 사용자에게 접근 권한이 없을 경우
     */
    @Transactional
    public WorkSessionResponse.InfoDTO createChatSession(WorkSessionRequest.CreateDTO request, Integer userId) {
        Workspace workspace = workspaceValidator.validateAndGetWorkspace(request.getWorkspaceId(), userId);

        ChatSession newChatSession = ChatSession.builder()
                .sessionTitle(request.getSessionTitle())
                .workspace(workspace)
                .build();

        chatSessionRepository.save(newChatSession);

        return new WorkSessionResponse.InfoDTO(newChatSession);
    }

    /**
     * 특정 채팅방을 조회합니다.
     *
     * @param sessionId 조회할 채팅방의 ID
     * @param workspaceId 채팅방이 속한 워크스페이스의 ID
     * @param userId 접근을 시도하는 사용자의 ID
     * @return 조회된 채팅방 정보 (InfoDTO)
     * @throws IllegalArgumentException 채팅방이 존재하지 않거나 접근 권한이 없을 경우
     */
    public WorkSessionResponse.InfoDTO getChatSession(Integer sessionId, Integer workspaceId, Integer userId) {
        ChatSession chatSession = workspaceValidator.validateAndGetChatSession(sessionId, workspaceId, userId);
        return new WorkSessionResponse.InfoDTO(chatSession);
    }
}


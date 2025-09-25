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
}


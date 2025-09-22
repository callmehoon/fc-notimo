package com.jober.final2teamdrhong.dto.worksession;

import com.jober.final2teamdrhong.entity.ChatSession;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class WorkSessionResponse {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class InfoDTO {
        private Integer chatSessionId;
        private String sessionTitle;
        private Integer workspaceId;
        private LocalDateTime createdAt;

        public InfoDTO(ChatSession chatSession) {
            this.chatSessionId = chatSession.getSessionId();
            this.sessionTitle = chatSession.getSessionTitle();
            this.workspaceId = chatSession.getWorkspace().getWorkspaceId();
            this.createdAt = chatSession.getCreatedAt();
        }
    }
}
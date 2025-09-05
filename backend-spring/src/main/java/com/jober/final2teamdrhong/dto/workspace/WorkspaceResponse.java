package com.jober.final2teamdrhong.dto.workspace;

import com.jober.final2teamdrhong.entity.Workspace;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 워크스페이스 관련 응답 DTO들을 모아두는 클래스입니다.
 */
public class WorkspaceResponse {

    /**
     * 워크스페이스 목록의 각 아이템 응답을 위한 DTO
     */
    @Getter
    public static class SimpleDTO {
        private Integer workspaceId;
        private String workspaceName;
        private String workspaceSubname;
        private LocalDateTime createdAt;

        /**
         * Workspace 엔티티를 SimpleDTO로 변환하는 생성자입니다.
         *
         * @param workspace 변환할 Workspace 엔티티 객체
         */
        public SimpleDTO(Workspace workspace) {
            this.workspaceId = workspace.getWorkspaceId();
            this.workspaceName = workspace.getWorkspaceName();
            this.workspaceSubname = workspace.getWorkspaceSubname();
            this.createdAt = workspace.getCreatedAt();
        }
    }
}

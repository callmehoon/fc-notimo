package com.jober.final2teamdrhong.dto.workspace;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        private final Integer workspaceId;
        private final String workspaceName;
        private final String workspaceSubname;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime updatedAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime deletedAt; // delete 동작시 반환되는 SimpleDTO에서 삭제된 시각을 알 수 없기 때문에 삭제시간 추가

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
            this.updatedAt = workspace.getUpdatedAt();
            this.deletedAt = workspace.getDeletedAt();
        }
    }
}

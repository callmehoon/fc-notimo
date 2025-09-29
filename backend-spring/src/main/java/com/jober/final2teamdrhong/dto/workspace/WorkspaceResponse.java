package com.jober.final2teamdrhong.dto.workspace;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jober.final2teamdrhong.entity.Workspace;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 워크스페이스 관련 응답 DTO들을 모아두는 클래스입니다.
 */
public class WorkspaceResponse {

    /**
     * 워크스페이스 목록의 각 아이템 응답을 위한 DTO
     */
    @Schema(name = "WorkspaceSimpleDTO")
    public record SimpleDTO(
        Integer workspaceId,
        String workspaceName,
        String workspaceSubname,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime deletedAt // delete 동작시 반환되는 SimpleDTO에서 삭제된 시각을 알 수 없기 때문에 삭제시간 추가
    ) {
        /**
         * Workspace 엔티티를 SimpleDTO로 변환하는 생성자입니다.
         *
         * @param workspace 변환할 Workspace 엔티티 객체
         */
        public SimpleDTO(Workspace workspace) {
            this(
                workspace.getWorkspaceId(),
                workspace.getWorkspaceName(),
                workspace.getWorkspaceSubname(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt(),
                workspace.getDeletedAt()
            );
        }
    }

    /**
     * 워크스페이스 상세 정보 응답을 위한 DTO
     */
    @Schema(name = "WorkspaceDetailDTO")
    public record DetailDTO(
        Integer workspaceId,
        String workspaceName,
        String workspaceSubname,
        String workspaceAddress,
        String workspaceDetailAddress,
        String workspaceUrl,
        String representerName,
        String representerPhoneNumber,
        String representerEmail,
        String companyName,
        String companyRegisterNumber,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt, // update 동작시 반환되는 DetailDTO에서 수정된 시각을 알 수 없기 때문에 수정시간 추가
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime deletedAt
    ) {
        /**
         * Workspace 엔티티를 DetailDTO로 변환하는 생성자입니다.
         *
         * @param workspace 변환할 Workspace 엔티티 객체
         */
        public DetailDTO(Workspace workspace) {
            this(
                workspace.getWorkspaceId(),
                workspace.getWorkspaceName(),
                workspace.getWorkspaceSubname(),
                workspace.getWorkspaceAddress(),
                workspace.getWorkspaceDetailAddress(),
                workspace.getWorkspaceUrl(),
                workspace.getRepresenterName(),
                workspace.getRepresenterPhoneNumber(),
                workspace.getRepresenterEmail(),
                workspace.getCompanyName(),
                workspace.getCompanyRegisterNumber(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt(),
                workspace.getDeletedAt()
            );
        }
    }
}

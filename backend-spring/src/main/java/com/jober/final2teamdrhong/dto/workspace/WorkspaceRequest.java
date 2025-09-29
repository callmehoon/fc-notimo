package com.jober.final2teamdrhong.dto.workspace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 * 워크스페이스 관련 요청 DTO들을 모아두는 클래스입니다.
 */
public class WorkspaceRequest {

    /**
     * 워크스페이스 생성을 요청하는 DTO 입니다.
     */
    @Schema(name = "WorkspaceCreateDTO")
    public record CreateDTO(
        @NotBlank(message = "워크스페이스 이름은 필수 입력 항목입니다.")
        @Length(min = 2, max = 20, message = "워크스페이스 이름은 2자 이상 20자 이하로 입력해주세요.")
        String workspaceName,
        String workspaceSubname,
        String workspaceAddress,
        String workspaceDetailAddress,
        @NotBlank(message = "고유 URL은 필수 입력 항목입니다.")
        String workspaceUrl,
        @NotBlank(message = "대표자 이름은 필수 입력 항목입니다.")
        String representerName,
        @NotBlank(message = "대표자 연락처는 필수 입력 항목입니다.")
        String representerPhoneNumber,
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String representerEmail,
        @NotBlank(message = "회사 이름은 필수 입력 항목입니다.")
        String companyName,
        String companyRegisterNumber
    ) {}

    /**
     * 워크스페이스 수정을 요청하는 DTO 입니다.
     */
    @Schema(name = "WorkspaceUpdateDTO")
    public record UpdateDTO(
        @NotBlank(message = "워크스페이스 이름은 필수 입력 항목입니다.")
        @Length(min = 2, max = 20, message = "워크스페이스 이름은 2자 이상 20자 이하로 입력해주세요.")
        String newWorkspaceName,
        String newWorkspaceSubname,
        String newWorkspaceAddress,
        String newWorkspaceDetailAddress,
        @NotBlank(message = "고유 URL은 필수 입력 항목입니다.")
        String newWorkspaceUrl,
        @NotBlank(message = "대표자 이름은 필수 입력 항목입니다.")
        String newRepresenterName,
        @NotBlank(message = "대표자 연락처는 필수 입력 항목입니다.")
        String newRepresenterPhoneNumber,
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String newRepresenterEmail,
        @NotBlank(message = "회사 이름은 필수 입력 항목입니다.")
        String newCompanyName,
        String newCompanyRegisterNumber
    ) {}
}

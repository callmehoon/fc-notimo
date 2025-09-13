package com.jober.final2teamdrhong.dto.workspace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * 워크스페이스 관련 요청 DTO들을 모아두는 클래스입니다.
 */
public class WorkspaceRequest {

    /**
     * 워크스페이스 생성을 요청하는 DTO 입니다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor // @Builder와 더불어, 함께 사용해서 명확하게 public 생성자를 열어줌
    @Builder // Test 코드 작성시 객체생성을 편리하게 하기 위해 @Builder 채택
    @Schema(name = "WorkspaceCreateDTO")
    public static class CreateDTO {
        @NotBlank(message = "워크스페이스 이름은 필수 입력 항목입니다.")
        @Length(min = 2, max = 20, message = "워크스페이스 이름은 2자 이상 20자 이하로 입력해주세요.")
        private String workspaceName;
        private String workspaceSubname;
        private String workspaceAddress;
        private String workspaceDetailAddress;
        @NotBlank(message = "고유 URL은 필수 입력 항목입니다.")
        private String workspaceUrl;
        @NotBlank(message = "대표자 이름은 필수 입력 항목입니다.")
        private String representerName;
        @NotBlank(message = "대표자 연락처는 필수 입력 항목입니다.")
        private String representerPhoneNumber;
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String representerEmail;
        @NotBlank(message = "회사 이름은 필수 입력 항목입니다.")
        private String companyName;
        private String companyRegisterNumber;
    }

    /**
     * 워크스페이스 수정을 요청하는 DTO 입니다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor // @Builder와 더불어, 함께 사용해서 명확하게 public 생성자를 열어줌
    @Builder // Test 코드 작성시 객체생성을 편리하게 하기 위해 @Builder 채택
    public static class UpdateDTO {
        @NotBlank(message = "워크스페이스 이름은 필수 입력 항목입니다.")
        @Length(min = 2, max = 20, message = "워크스페이스 이름은 2자 이상 20자 이하로 입력해주세요.")
        private String newWorkspaceName;
        private String newWorkspaceSubname;
        private String newWorkspaceAddress;
        private String newWorkspaceDetailAddress;
        @NotBlank(message = "고유 URL은 필수 입력 항목입니다.")
        private String newWorkspaceUrl;
        @NotBlank(message = "대표자 이름은 필수 입력 항목입니다.")
        private String newRepresenterName;
        @NotBlank(message = "대표자 연락처는 필수 입력 항목입니다.")
        private String newRepresenterPhoneNumber;
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String newRepresenterEmail;
        @NotBlank(message = "회사 이름은 필수 입력 항목입니다.")
        private String newCompanyName;
        private String newCompanyRegisterNumber;
    }
}

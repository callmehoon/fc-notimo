package com.jober.final2teamdrhong.dto.worksession;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class WorkSessionRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO {
        @NotNull(message = "workspaceID는 NULL일 수 없습니다.")
        private Integer workspaceId;

        @NotBlank(message = "채팅방 이름은 필수 입력 항목입니다.")
        private String sessionTitle;
    }
}
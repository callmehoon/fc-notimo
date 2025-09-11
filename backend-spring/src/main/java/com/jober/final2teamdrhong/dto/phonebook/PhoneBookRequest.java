package com.jober.final2teamdrhong.dto.phonebook;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * 주소록(PhoneBook) 관련 요청 DTO들을 모아두는 클래스입니다.
 */
public class PhoneBookRequest {

    /**
     * 주소록 생성을 위한 요청 DTO 입니다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "PhoneBookCreateRequest")
    public static class CreateDTO {
        @NotBlank(message = "주소록 이름은 필수 입력 항목입니다.")
        @Length(min = 2, max = 20, message = "주소록 이름은 2자 이상 20자 이하로 입력해주세요.")
        private String phoneBookName;
        private String phoneBookMemo;
    }
}

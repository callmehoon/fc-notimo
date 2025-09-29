package com.jober.final2teamdrhong.dto.recipient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 * 수신자(Recipient) 관련 요청 DTO들을 모아두는 클래스입니다.
 */
public class RecipientRequest {

    /**
     * 수신자 생성을 위한 요청 DTO 입니다.
     */
    @Schema(name = "RecipientCreateDTO")
    public record CreateDTO(
        @NotBlank(message = "수신인 이름은 필수 입력 항목입니다.")
        @Length(min = 2, max = 10, message = "수신인 이름은 2자 이상 10자 이하로 입력해주세요.")
        String recipientName,
        @NotBlank(message = "수신인 연락처는 필수 입력 항목입니다.")
        String recipientPhoneNumber,
        @Length(max = 1000, message = "메모는 1000자 이하로 입력해주세요.")
        String recipientMemo
    ) {}

    /**
     * 수신자 정보 수정을 위한 요청 DTO 입니다.
     */
    @Schema(name = "RecipientUpdateDTO")
    public record UpdateDTO(
        @NotBlank(message = "수신인 이름은 필수 입력 항목입니다.")
        @Length(min = 2, max = 10, message = "수신인 이름은 2자 이상 10자 이하로 입력해주세요.")
        String newRecipientName,
        @NotBlank(message = "수신인 연락처는 필수 입력 항목입니다.")
        String newRecipientPhoneNumber,
        @Length(max = 1000, message = "메모는 1000자 이하로 입력해주세요.")
        String newRecipientMemo
    ) {}
}

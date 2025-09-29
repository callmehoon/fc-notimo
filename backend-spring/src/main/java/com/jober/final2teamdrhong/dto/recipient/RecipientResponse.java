package com.jober.final2teamdrhong.dto.recipient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jober.final2teamdrhong.entity.Recipient;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 수신자(Recipient) 관련 응답 DTO들을 모아두는 클래스입니다.
 */
public class RecipientResponse {

    /**
     * 수신자의 기본 정보 응답을 위한 DTO
     */
    @Schema(name = "RecipientSimpleDTO")
    public record SimpleDTO(
        Integer recipientId,
        String recipientName,
        String recipientPhoneNumber,
        String recipientMemo,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime deletedAt
    ) {
        /**
         * Recipient 엔티티를 SimpleDTO로 변환하는 생성자입니다.
         *
         * @param recipient 변환할 Recipient 엔티티 객체
         */
        public SimpleDTO(Recipient recipient) {
            this(
                recipient.getRecipientId(),
                recipient.getRecipientName(),
                recipient.getRecipientPhoneNumber(),
                recipient.getRecipientMemo(),
                recipient.getCreatedAt(),
                recipient.getUpdatedAt(),
                recipient.getDeletedAt()
            );
        }
    }
}

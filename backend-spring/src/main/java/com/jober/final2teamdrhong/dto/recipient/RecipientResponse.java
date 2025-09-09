package com.jober.final2teamdrhong.dto.recipient;

import com.jober.final2teamdrhong.entity.Recipient;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 수신자(Recipient) 관련 응답 DTO들을 모아두는 클래스입니다.
 */
public class RecipientResponse {

    /**
     * 수신자의 기본 정보 응답을 위한 DTO
     */
    @Getter
    public static class SimpleDTO {
        private Integer recipientId;
        private String recipientName;
        private String recipientPhoneNumber;
        private String recipientMemo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        /**
         * Recipient 엔티티를 SimpleDTO로 변환하는 생성자입니다.
         *
         * @param recipient 변환할 Recipient 엔티티 객체
         */
        public SimpleDTO(Recipient recipient) {
            this.recipientId = recipient.getRecipientId();
            this.recipientName = recipient.getRecipientName();
            this.recipientPhoneNumber = recipient.getRecipientPhoneNumber();
            this.recipientMemo = recipient.getRecipientMemo();
            this.createdAt = recipient.getCreatedAt();
            this.updatedAt = recipient.getUpdatedAt();
            this.deletedAt = recipient.getDeletedAt();
        }
    }
}

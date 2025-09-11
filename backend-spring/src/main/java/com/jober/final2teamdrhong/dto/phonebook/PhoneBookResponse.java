package com.jober.final2teamdrhong.dto.phonebook;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jober.final2teamdrhong.entity.PhoneBook;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주소록(PhoneBook) 관련 응답 DTO들을 모아두는 클래스입니다.
 */
public class PhoneBookResponse {

    /**
     * 주소록의 기본 정보 응답을 위한 DTO
     */
    @Getter
    public static class SimpleDTO {
        private final Integer phoneBookId;
        private final String phoneBookName;
        private final String phoneBookMemo;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime updatedAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime deletedAt;

        /**
         * PhoneBook 엔티티를 SimpleDTO로 변환하는 생성자입니다.
         *
         * @param phoneBook 변환할 PhoneBook 엔티티 객체
         */
        public SimpleDTO(PhoneBook phoneBook) {
            this.phoneBookId = phoneBook.getPhoneBookId();
            this.phoneBookName = phoneBook.getPhoneBookName();
            this.phoneBookMemo = phoneBook.getPhoneBookMemo();
            this.createdAt = phoneBook.getCreatedAt();
            this.updatedAt = phoneBook.getUpdatedAt();
            this.deletedAt = phoneBook.getDeletedAt();
        }
    }
}

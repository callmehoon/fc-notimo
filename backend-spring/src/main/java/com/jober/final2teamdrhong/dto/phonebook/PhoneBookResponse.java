package com.jober.final2teamdrhong.dto.phonebook;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.jober.final2teamdrhong.dto.recipient.RecipientResponse;
import com.jober.final2teamdrhong.entity.GroupMapping;
import com.jober.final2teamdrhong.entity.PhoneBook;
import com.jober.final2teamdrhong.entity.Recipient;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 주소록(PhoneBook) 관련 응답 DTO들을 모아두는 클래스입니다.
 */
public class PhoneBookResponse {

    /**
     * 주소록의 기본 정보 응답을 위한 DTO
     */
    @Schema(name = "PhoneBookSimpleDTO")
    public record SimpleDTO(
        Integer phoneBookId,
        String phoneBookName,
        String phoneBookMemo,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime deletedAt
    ) {
        /**
         * PhoneBook 엔티티를 SimpleDTO로 변환하는 생성자입니다.
         *
         * @param phoneBook 변환할 PhoneBook 엔티티 객체
         */
        public SimpleDTO(PhoneBook phoneBook) {
            this(
                phoneBook.getPhoneBookId(),
                phoneBook.getPhoneBookName(),
                phoneBook.getPhoneBookMemo(),
                phoneBook.getCreatedAt(),
                phoneBook.getUpdatedAt(),
                phoneBook.getDeletedAt()
            );
        }
    }

    /**
     * 주소록에 다수의 수신자를 추가 또는 삭제한 후의 응답을 위한 DTO
     */
    @Schema(name = "PhoneBookModifiedRecipientsDTO")
    @JsonPropertyOrder({ "phoneBookId", "phoneBookName", "createdAt", "updatedAt", "deletedAt", "recipientList" })
    public record ModifiedRecipientsDTO(
        Integer phoneBookId,
        String phoneBookName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime deletedAt,
        List<RecipientResponse.SimpleDTO> recipientList
    ) {
        /**
         * DTO의 일관된 생성을 보장하기 위한 private 정적 팩토리 헬퍼 메소드입니다.
         * 외부에서는 반드시 공개된 정적 팩토리 메소드(ofAddition, ofDeletion)를 통해 객체를 생성해야 합니다.
         *
         * @param phoneBook  작업 대상 주소록
         * @param createdAt  생성 시간 (추가 이벤트용)
         * @param updatedAt  수정 시간 (모든 이벤트용)
         * @param deletedAt  삭제 시간 (삭제 이벤트용)
         * @param modifiedRecipients 변경된 수신자 목록
         * @return 모든 필드가 채워진 ModifiedRecipientsDTO 레코드 객체
         */
        private static ModifiedRecipientsDTO from(PhoneBook phoneBook,
                                                        LocalDateTime createdAt,
                                                        LocalDateTime updatedAt,
                                                        LocalDateTime deletedAt,
                                                        List<Recipient> modifiedRecipients) {
            return new ModifiedRecipientsDTO(
                phoneBook.getPhoneBookId(),
                phoneBook.getPhoneBookName(),
                createdAt,
                updatedAt,
                deletedAt,
                modifiedRecipients.stream()
                        .map(RecipientResponse.SimpleDTO::new)
                        .toList()
            );
        }

        /**
         * '수신자 추가' 이벤트에 대한 DTO를 생성합니다.
         * createdAt과 updatedAt은 DB에 저장된 GroupMapping의 생성 시간을 사용합니다.
         *
         * @param phoneBook     작업 대상 주소록 엔티티
         * @param savedMappings DB에 저장된 후의 GroupMapping 엔티티 리스트 (createdAt 포함)
         * @return '추가' 이벤트의 정보가 채워진 ModifiedRecipientsDTO 객체
         */
        public static ModifiedRecipientsDTO ofAddition(PhoneBook phoneBook, List<GroupMapping> savedMappings) {
            LocalDateTime timestamp =
                    savedMappings.isEmpty() ? ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime() : savedMappings.getFirst().getCreatedAt();

            List<Recipient> addedRecipients = savedMappings.stream()
                    .map(GroupMapping::getRecipient)
                    .toList();

            return from(phoneBook, timestamp, timestamp, null, addedRecipients);
        }

        /**
         * '수신자 삭제' 이벤트에 대한 DTO를 생성합니다.
         * updatedAt과 deletedAt은 서비스 계층에서 재조회한, DB에 실제로 기록된 GroupMapping의 삭제 시간을 사용합니다.
         *
         * @param phoneBook       작업 대상 주소록 엔티티
         * @param deletedMappings DB에서 재조회한, 소프트 딜리트 처리된 GroupMapping 엔티티 리스트
         * @return '삭제' 이벤트의 정보가 채워진 ModifiedRecipientsDTO 객체
         */
        public static ModifiedRecipientsDTO ofDeletion(PhoneBook phoneBook, List<GroupMapping> deletedMappings) {
            LocalDateTime timestamp =
                    deletedMappings.isEmpty() ? ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime() : deletedMappings.getFirst().getDeletedAt();

            List<Recipient> removedRecipients = deletedMappings.stream()
                    .map(GroupMapping::getRecipient)
                    .toList();

            return from(phoneBook, null, timestamp, timestamp, removedRecipients);
        }
    }
}

package com.jober.final2teamdrhong.dto.individualtemplate;

import com.jober.final2teamdrhong.entity.TemplateModifiedHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class HistoryResponse {

    private final Integer historyId;
    private final String chatUser;
    private final String chatAi;
    private final LocalDateTime createdAt;

    @Builder
    public HistoryResponse(Integer historyId, String chatUser, String chatAi, LocalDateTime createdAt) {
        this.historyId = historyId;
        this.chatUser = chatUser;
        this.chatAi = chatAi;
        this.createdAt = createdAt;
    }

    // 엔티티를 DTO로 변환하는 정적 팩토리 메소드
    public static HistoryResponse fromEntity(TemplateModifiedHistory history) {
        return HistoryResponse.builder()
                .historyId(history.getHistoryId())
                .chatUser(history.getChatUser())
                .chatAi(history.getChatAi())
                .createdAt(history.getCreatedAt())
                .build();
    }
}

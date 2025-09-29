package com.jober.final2teamdrhong.dto.individualtemplate;

import com.jober.final2teamdrhong.entity.TemplateModifiedHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryResponse {

    private final Integer historyId;
    private final String chatUser;
    private final String chatAi;
    private final String individualTemplateTitle;
    private final String individualTemplateContent;
    private final String buttonTitle;
    private final LocalDateTime createdAt;

    public static HistoryResponse fromEntity(TemplateModifiedHistory history) {
        return new HistoryResponse(
                history.getHistoryId(),
                history.getChatUser(),
                history.getChatAi(),
                history.getHistoryTitle(),
                history.getHistoryContent(),
                history.getButtonTitle(),
                history.getCreatedAt()
        );
    }
}

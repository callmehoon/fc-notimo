package com.jober.final2teamdrhong.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이메일 인증 코드 발송 용도를 정의하는 Enum
 * 각 용도에 맞는 이메일 제목과 설명을 제공합니다.
 */
@Getter
@RequiredArgsConstructor
public enum EmailPurpose {
    SIGNUP("회원가입", "회원가입을 위해"),
    PASSWORD_RESET("비밀번호 재설정", "비밀번호 재설정을 위해"),
    ACCOUNT_MERGE("계정 통합", "계정 통합을 위해");

    private final String title;
    private final String description;

    /**
     * 이메일 제목을 생성합니다.
     *
     * @return "[notimo] {용도} 인증 코드입니다." 형식의 제목
     */
    public String getEmailSubject() {
        return String.format("[notimo] %s 인증 코드입니다.", this.title);
    }

    /**
     * 이메일 본문 내용을 생성합니다.
     *
     * @param code 인증 코드
     * @param expiryMinutes 만료 시간(분)
     * @return 포맷팅된 이메일 본문
     */
    public String getEmailContent(String code, int expiryMinutes) {
        return String.format(
            "%s 아래 인증 코드를 입력해주세요.\n\n" +
            "인증 코드: %s\n\n" +
            "이 코드는 %d분 후에 만료됩니다.\n" +
            "만약 본인이 요청하지 않았다면 이 메일을 무시해주세요.",
            this.description, code, expiryMinutes
        );
    }
}
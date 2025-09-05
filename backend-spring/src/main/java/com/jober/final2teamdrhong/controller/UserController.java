package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.EmailRequestDto;
import com.jober.final2teamdrhong.dto.UserSignupRequestDto;
import com.jober.final2teamdrhong.dto.UserSignupResponseDto;
import com.jober.final2teamdrhong.service.EmailService;
import com.jober.final2teamdrhong.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/send-verification-code")
    public ResponseEntity<UserSignupResponseDto> sendVerificationCode(
            @Valid @RequestBody EmailRequestDto emailRequestDto) {
        try {
            emailService.sendVerificationCode(emailRequestDto.getEmail());
            return ResponseEntity.ok(
                UserSignupResponseDto.success("인증 코드가 발송되었습니다.")
            );
        } catch (Exception e) {
            log.error("인증 코드 발송 실패: email={}, error={}", emailRequestDto.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(
                UserSignupResponseDto.failure(e.getMessage())
            );
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(
            @Valid @RequestBody UserSignupRequestDto userSignupRequestDto) {
        try {
            userService.signup(userSignupRequestDto);
            return ResponseEntity.ok(
                UserSignupResponseDto.success("회원가입이 성공적으로 완료되었습니다.")
            );
        } catch (Exception e) {
            log.error("회원가입 실패: email={}, error={}", userSignupRequestDto.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(
                UserSignupResponseDto.failure(e.getMessage())
            );
        }
    }
}

package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.EmailVerification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmailVerificationRepositoryTest {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private EmailVerification testEmailVerification;

    @BeforeEach
    void setUp() {
        // 1. 테스트용 이메일 인증 정보를 생성하고 저장합니다.
        testEmailVerification = EmailVerification.create("test@example.com", "123456", 5);
        entityManager.persist(testEmailVerification);

        // 2. 변경사항을 DB에 반영하고 영속성 컨텍스트를 비웁니다.
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("이메일로 인증 정보 조회 성공 테스트")
    void findByEmail_Success_Test() {
        // given
        // 1. 테스트에 사용할 이메일을 준비합니다.
        String testEmail = "test@example.com";

        // when
        // 1. 이메일로 인증 정보를 조회합니다.
        Optional<EmailVerification> result = emailVerificationRepository.findByEmail(testEmail);

        // then
        // 1. 조회 결과가 존재하는지 확인합니다.
        assertThat(result).isPresent();
        // 2. 조회된 인증 정보의 내용이 정확한지 검증합니다.
        EmailVerification foundVerification = result.get();
        assertThat(foundVerification.getEmail()).isEqualTo(testEmail);
        assertThat(foundVerification.getVerificationCode()).isEqualTo("123456");
    }

    @Test
    @DisplayName("이메일로 인증 정보 조회 실패 테스트 - 존재하지 않는 이메일")
    void findByEmail_Fail_EmailNotFound_Test() {
        // given
        // 1. 존재하지 않는 이메일을 준비합니다.
        String nonExistingEmail = "notexist@example.com";

        // when
        // 1. 존재하지 않는 이메일로 인증 정보를 조회합니다.
        Optional<EmailVerification> result = emailVerificationRepository.findByEmail(nonExistingEmail);

        // then
        // 1. 조회 결과가 비어있는지 확인합니다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일 인증 정보 저장 성공 테스트")
    void save_Success_Test() {
        // given
        // 1. 새로운 이메일 인증 정보를 생성합니다.
        EmailVerification newVerification = EmailVerification.create("newuser@example.com", "789012", 10);

        // when
        // 1. 이메일 인증 정보를 저장합니다.
        EmailVerification savedVerification = emailVerificationRepository.save(newVerification);

        // then
        // 1. 저장된 인증 정보에 ID가 할당되었는지 확인합니다.
        assertThat(savedVerification.getVerificationId()).isNotNull();
        // 2. 저장된 인증 정보의 내용이 정확한지 검증합니다.
        assertThat(savedVerification.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedVerification.getVerificationCode()).isEqualTo("789012");
        assertThat(savedVerification.getExpiresAt()).isAfter(LocalDateTime.now());

        // 3. 실제로 DB에 저장되었는지 다시 조회하여 확인합니다.
        Optional<EmailVerification> foundVerification = emailVerificationRepository.findByEmail("newuser@example.com");
        assertThat(foundVerification).isPresent();
        assertThat(foundVerification.get().getVerificationCode()).isEqualTo("789012");
    }

    @Test
    @DisplayName("이메일로 인증 정보 삭제 성공 테스트")
    void deleteByEmail_Success_Test() {
        // given
        // 1. 삭제할 이메일을 준비합니다.
        String emailToDelete = "test@example.com";

        // 2. 삭제 전에 인증 정보가 존재하는지 확인합니다.
        assertThat(emailVerificationRepository.findByEmail(emailToDelete)).isPresent();

        // when
        // 1. 이메일로 인증 정보를 삭제합니다.
        emailVerificationRepository.deleteByEmail(emailToDelete);
        entityManager.flush(); // 삭제를 즉시 DB에 반영

        // then
        // 1. 인증 정보가 삭제되었는지 확인합니다.
        Optional<EmailVerification> deletedVerification = emailVerificationRepository.findByEmail(emailToDelete);
        assertThat(deletedVerification).isEmpty();
    }

    @Test
    @DisplayName("기본값으로 이메일 인증 정보 생성 테스트")
    void createWithDefaultValidity_Success_Test() {
        // given
        // 1. 기본 유효시간으로 인증 정보를 생성합니다.
        String testEmail = "default@example.com";
        String testCode = "DEFAULT123";

        // when
        // 1. 기본값을 사용하여 인증 정보를 생성하고 저장합니다.
        EmailVerification verification = EmailVerification.create(testEmail, testCode);
        EmailVerification savedVerification = emailVerificationRepository.save(verification);

        // then
        // 1. 저장된 인증 정보의 내용이 정확한지 검증합니다.
        assertThat(savedVerification.getEmail()).isEqualTo(testEmail);
        assertThat(savedVerification.getVerificationCode()).isEqualTo(testCode);
        // 2. 만료시간이 현재 시간보다 미래인지 확인합니다.
        assertThat(savedVerification.getExpiresAt()).isAfter(LocalDateTime.now());
        // 3. 기본 상태에서는 유효한지 확인합니다.
        assertThat(savedVerification.isValid()).isTrue();
        assertThat(savedVerification.isExpired()).isFalse();
    }

    @Test
    @DisplayName("만료된 인증 정보 유효성 검증 테스트")
    void isExpired_True_Test() {
        // given
        // 1. 만료 시간을 과거로 설정한 인증 정보를 생성합니다.
        EmailVerification expiredVerification = EmailVerification.builder()
                .email("expired@example.com")
                .verificationCode("EXPIRED123")
                .expiresAt(LocalDateTime.now().minusMinutes(1)) // 1분 전에 만료
                .build();

        EmailVerification savedVerification = emailVerificationRepository.save(expiredVerification);

        // when & then
        // 1. 만료된 인증 정보가 올바르게 만료 상태로 판단되는지 확인합니다.
        assertThat(savedVerification.isExpired()).isTrue();
        assertThat(savedVerification.isValid()).isFalse();
    }
}
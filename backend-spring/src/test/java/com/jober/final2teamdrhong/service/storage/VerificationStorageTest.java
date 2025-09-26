package com.jober.final2teamdrhong.service.storage;

import com.jober.final2teamdrhong.entity.EmailVerification;
import com.jober.final2teamdrhong.repository.EmailVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * VerificationStorage 구현체들 테스트
 */
@ExtendWith(MockitoExtension.class)
class VerificationStorageTest {

    // 테스트 상수
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CODE = "123456";
    private static final String WRONG_CODE = "999999";
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);

    @Nested
    @DisplayName("RedisVerificationStorage 테스트")
    class RedisVerificationStorageTest {

        @Mock
        private StringRedisTemplate redisTemplate;

        @Mock
        private ValueOperations<String, String> valueOperations;


        @InjectMocks
        private RedisVerificationStorage redisStorage;

        @BeforeEach
        void setUp() {
            lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("Redis에 인증 코드 저장 성공 (5분 TTL)")
        void save_ValidEmailAndCode_SavesWithExpiration() {
            // when
            redisStorage.save(TEST_EMAIL, TEST_CODE);

            // then
            verify(valueOperations).set(TEST_EMAIL, TEST_CODE, CODE_EXPIRATION);
        }

        @Test
        @DisplayName("Redis에서 인증 코드 조회 성공")
        void find_ExistingCode_ReturnsValue() {
            // given
            given(valueOperations.get(TEST_EMAIL)).willReturn(TEST_CODE);

            // when
            Optional<String> result = redisStorage.find(TEST_EMAIL);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(TEST_CODE);
            verify(valueOperations).get(TEST_EMAIL);
        }

        @Test
        @DisplayName("Redis에서 존재하지 않는 키 조회 시 빈 결과")
        void find_NonExistingKey_ReturnsEmpty() {
            // given
            given(valueOperations.get(TEST_EMAIL)).willReturn(null);

            // when
            Optional<String> result = redisStorage.find(TEST_EMAIL);

            // then
            assertThat(result).isEmpty();
            verify(valueOperations).get(TEST_EMAIL);
        }

        @Test
        @DisplayName("Redis에서 인증 코드 삭제 성공")
        void delete_ExistingKey_DeletesSuccessfully() {
            // when
            redisStorage.delete(TEST_EMAIL);

            // then
            verify(redisTemplate).delete(TEST_EMAIL);
        }

        @Test
        @DisplayName("Lua 스크립트로 원자적 검증 및 삭제 성공")
        void validateAndDelete_CorrectCode_ReturnsTrue() {
            // given
            String expectedLuaScript = """
            local value = redis.call('GET', KEYS[1])
            if value == ARGV[1] then
                redis.call('DEL', KEYS[1])
                return 1
            else
                return 0
            end
            """;

            given(redisTemplate.execute(any(), eq(true))).willAnswer(invocation -> {
                // Lua 스크립트 실행 시뮬레이션
                return 1L; // 성공
            });

            // when
            boolean result = redisStorage.validateAndDelete(TEST_EMAIL, TEST_CODE);

            // then
            assertThat(result).isTrue();
            verify(redisTemplate).execute(any(), eq(true));
        }

        @Test
        @DisplayName("Lua 스크립트로 잘못된 코드 검증 시 삭제하지 않음")
        void validateAndDelete_WrongCode_ReturnsFalse() {
            // given
            given(redisTemplate.execute(any(), eq(true))).willReturn(0L); // 실패

            // when
            boolean result = redisStorage.validateAndDelete(TEST_EMAIL, WRONG_CODE);

            // then
            assertThat(result).isFalse();
            verify(redisTemplate).execute(any(), eq(true));
        }

        @Test
        @DisplayName("Lua 스크립트 실행 결과가 null인 경우 false 반환")
        void validateAndDelete_NullResult_ReturnsFalse() {
            // given
            given(redisTemplate.execute(any(), eq(true))).willReturn(null);

            // when
            boolean result = redisStorage.validateAndDelete(TEST_EMAIL, TEST_CODE);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("RdbVerificationStorage 테스트")
    class RdbVerificationStorageTest {

        @Mock
        private EmailVerificationRepository repository;

        @InjectMocks
        private RdbVerificationStorage rdbStorage;

        private EmailVerification validVerification;
        private EmailVerification expiredVerification;

        @BeforeEach
        void setUp() {
            validVerification = EmailVerification.builder()
                    .email(TEST_EMAIL)
                    .verificationCode(TEST_CODE)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            expiredVerification = EmailVerification.builder()
                    .email(TEST_EMAIL)
                    .verificationCode(TEST_CODE)
                    .expiresAt(LocalDateTime.now().minusMinutes(1)) // 만료됨
                    .build();
        }

        @Test
        @DisplayName("RDB에 인증 코드 저장 성공 (기존 데이터 삭제 후)")
        void save_ValidEmailAndCode_DeletesOldAndSavesNew() {
            // when
            rdbStorage.save(TEST_EMAIL, TEST_CODE);

            // then
            verify(repository).deleteByEmail(TEST_EMAIL);
            verify(repository).flush(); // 즉시 삭제 반영
            verify(repository).save(any(EmailVerification.class));
        }

        @Test
        @DisplayName("RDB에서 유효한 인증 코드 조회 성공")
        void find_ValidVerification_ReturnsCode() {
            // given
            given(repository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(validVerification));

            // when
            Optional<String> result = rdbStorage.find(TEST_EMAIL);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(TEST_CODE);
            verify(repository).findByEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("RDB에서 만료된 인증 코드 조회 시 빈 결과")
        void find_ExpiredVerification_ReturnsEmpty() {
            // given
            given(repository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(expiredVerification));

            // when
            Optional<String> result = rdbStorage.find(TEST_EMAIL);

            // then
            assertThat(result).isEmpty();
            verify(repository).findByEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("RDB에서 존재하지 않는 인증 코드 조회 시 빈 결과")
        void find_NonExistingVerification_ReturnsEmpty() {
            // given
            given(repository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

            // when
            Optional<String> result = rdbStorage.find(TEST_EMAIL);

            // then
            assertThat(result).isEmpty();
            verify(repository).findByEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("RDB에서 인증 코드 삭제 성공")
        void delete_ExistingEmail_DeletesSuccessfully() {
            // when
            rdbStorage.delete(TEST_EMAIL);

            // then
            verify(repository).deleteByEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("트랜잭션 기반 검증 및 삭제 성공")
        void validateAndDelete_ValidCodeAndNotExpired_ReturnsTrue() {
            // given
            given(repository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(validVerification));

            // when
            boolean result = rdbStorage.validateAndDelete(TEST_EMAIL, TEST_CODE);

            // then
            assertThat(result).isTrue();
            verify(repository).findByEmail(TEST_EMAIL);
            verify(repository).deleteByEmail(TEST_EMAIL);
            verify(repository).flush(); // 즉시 반영
        }

        @Test
        @DisplayName("만료된 인증 코드 검증 시 삭제하지 않음")
        void validateAndDelete_ExpiredCode_ReturnsFalse() {
            // given
            given(repository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(expiredVerification));

            // when
            boolean result = rdbStorage.validateAndDelete(TEST_EMAIL, TEST_CODE);

            // then
            assertThat(result).isFalse();
            verify(repository).findByEmail(TEST_EMAIL);
            verify(repository, never()).deleteByEmail(anyString());
        }

        @Test
        @DisplayName("잘못된 인증 코드 검증 시 삭제하지 않음")
        void validateAndDelete_WrongCode_ReturnsFalse() {
            // given
            given(repository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(validVerification));

            // when
            boolean result = rdbStorage.validateAndDelete(TEST_EMAIL, WRONG_CODE);

            // then
            assertThat(result).isFalse();
            verify(repository).findByEmail(TEST_EMAIL);
            verify(repository, never()).deleteByEmail(anyString());
        }

        @Test
        @DisplayName("존재하지 않는 인증 코드 검증 시 false")
        void validateAndDelete_NonExistingCode_ReturnsFalse() {
            // given
            given(repository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

            // when
            boolean result = rdbStorage.validateAndDelete(TEST_EMAIL, TEST_CODE);

            // then
            assertThat(result).isFalse();
            verify(repository).findByEmail(TEST_EMAIL);
            verify(repository, never()).deleteByEmail(anyString());
        }
    }

    @Nested
    @DisplayName("FallbackVerificationStorage 테스트")
    class FallbackVerificationStorageTest {

        @Mock
        private VerificationStorage primaryStorage; // Redis

        @Mock
        private VerificationStorage secondaryStorage; // RDB

        @InjectMocks
        private FallbackVerificationStorage fallbackStorage;

        @BeforeEach
        void setUp() {
            // 생성자를 통해 의존성 주입
            fallbackStorage = new FallbackVerificationStorage(primaryStorage, secondaryStorage);
        }

        @Test
        @DisplayName("주 저장소(Redis) 정상 동작 시 주 저장소만 사용")
        void save_PrimaryStorageWorking_UsesPrimaryOnly() {
            // given - 주 저장소는 정상 동작
            willDoNothing().given(primaryStorage).save(TEST_EMAIL, TEST_CODE);

            // when
            fallbackStorage.save(TEST_EMAIL, TEST_CODE);

            // then
            verify(primaryStorage).save(TEST_EMAIL, TEST_CODE);
            verify(secondaryStorage, never()).save(anyString(), anyString());
        }

        @Test
        @DisplayName("주 저장소(Redis) 장애 시 예비 저장소(RDB)로 자동 전환")
        void save_PrimaryStorageFails_FallsBackToSecondary() {
            // given - 주 저장소 장애 발생
            DataAccessException exception = mock(DataAccessException.class);
            given(exception.getMessage()).willReturn("Redis connection failed");
            willThrow(exception).given(primaryStorage).save(TEST_EMAIL, TEST_CODE);

            // when
            fallbackStorage.save(TEST_EMAIL, TEST_CODE);

            // then
            verify(primaryStorage).save(TEST_EMAIL, TEST_CODE);
            verify(secondaryStorage).save(TEST_EMAIL, TEST_CODE);
        }

        @Test
        @DisplayName("주 저장소에서 값 찾기 성공 시 예비 저장소 확인하지 않음")
        void find_PrimaryStorageHasValue_ReturnsFromPrimary() {
            // given
            given(primaryStorage.find(TEST_EMAIL)).willReturn(Optional.of(TEST_CODE));

            // when
            Optional<String> result = fallbackStorage.find(TEST_EMAIL);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(TEST_CODE);
            verify(primaryStorage).find(TEST_EMAIL);
            verify(secondaryStorage, never()).find(anyString());
        }

        @Test
        @DisplayName("주 저장소에 값이 없을 때 예비 저장소에서 조회")
        void find_PrimaryStorageEmpty_ChecksSecondary() {
            // given
            given(primaryStorage.find(TEST_EMAIL)).willReturn(Optional.empty());
            given(secondaryStorage.find(TEST_EMAIL)).willReturn(Optional.of(TEST_CODE));

            // when
            Optional<String> result = fallbackStorage.find(TEST_EMAIL);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(TEST_CODE);
            verify(primaryStorage).find(TEST_EMAIL);
            verify(secondaryStorage).find(TEST_EMAIL);
        }

        @Test
        @DisplayName("주 저장소 장애 시 예비 저장소에서만 조회")
        void find_PrimaryStorageFails_FallsBackToSecondary() {
            // given
            DataAccessException exception = mock(DataAccessException.class);
            given(exception.getMessage()).willReturn("Redis connection failed");
            given(primaryStorage.find(TEST_EMAIL)).willThrow(exception);
            given(secondaryStorage.find(TEST_EMAIL)).willReturn(Optional.of(TEST_CODE));

            // when
            Optional<String> result = fallbackStorage.find(TEST_EMAIL);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(TEST_CODE);
            verify(primaryStorage).find(TEST_EMAIL);
            verify(secondaryStorage).find(TEST_EMAIL);
        }

        @Test
        @DisplayName("양쪽 저장소 모두에 값이 없는 경우")
        void find_BothStoragesEmpty_ReturnsEmpty() {
            // given
            given(primaryStorage.find(TEST_EMAIL)).willReturn(Optional.empty());
            given(secondaryStorage.find(TEST_EMAIL)).willReturn(Optional.empty());

            // when
            Optional<String> result = fallbackStorage.find(TEST_EMAIL);

            // then
            assertThat(result).isEmpty();
            verify(primaryStorage).find(TEST_EMAIL);
            verify(secondaryStorage).find(TEST_EMAIL);
        }

        @Test
        @DisplayName("삭제 시 양쪽 저장소 모두에서 시도")
        void delete_BothStorages_DeletesFromBoth() {
            // given - 주 저장소는 정상, 예비 저장소도 정상
            willDoNothing().given(primaryStorage).delete(TEST_EMAIL);
            willDoNothing().given(secondaryStorage).delete(TEST_EMAIL);

            // when
            fallbackStorage.delete(TEST_EMAIL);

            // then
            verify(primaryStorage).delete(TEST_EMAIL);
            verify(secondaryStorage).delete(TEST_EMAIL);
        }

        @Test
        @DisplayName("주 저장소 삭제 실패해도 예비 저장소 삭제는 계속 진행")
        void delete_PrimaryStorageFails_ContinuesWithSecondary() {
            // given
            DataAccessException primaryException = mock(DataAccessException.class);
            given(primaryException.getMessage()).willReturn("Redis connection failed");
            willThrow(primaryException).given(primaryStorage).delete(TEST_EMAIL);

            // when
            fallbackStorage.delete(TEST_EMAIL);

            // then
            verify(primaryStorage).delete(TEST_EMAIL);
            verify(secondaryStorage).delete(TEST_EMAIL);
        }

        @Test
        @DisplayName("예비 저장소 삭제 실패는 로그만 남기고 정상 진행")
        void delete_SecondaryStorageFails_LogsAndContinues() {
            // given
            DataAccessException secondaryException = mock(DataAccessException.class);
            given(secondaryException.getMessage()).willReturn("DB connection failed");
            willThrow(secondaryException).given(secondaryStorage).delete(TEST_EMAIL);

            // when - 예외가 발생하지 않아야 함
            assertThatCode(() -> fallbackStorage.delete(TEST_EMAIL))
                    .doesNotThrowAnyException();

            // then
            verify(primaryStorage).delete(TEST_EMAIL);
            verify(secondaryStorage).delete(TEST_EMAIL);
        }
    }

    @Nested
    @DisplayName("인터페이스 기본 구현 메서드 테스트")
    class DefaultMethodTest {

        // 테스트용 간단한 구현체
        private static class TestVerificationStorage implements VerificationStorage {
            private String storedValue;

            @Override
            public void save(String key, String value) {
                this.storedValue = value;
            }

            @Override
            public Optional<String> find(String key) {
                return Optional.ofNullable(storedValue);
            }

            @Override
            public void delete(String key) {
                this.storedValue = null;
            }
        }

        private TestVerificationStorage testStorage;

        @BeforeEach
        void setUp() {
            testStorage = new TestVerificationStorage();
        }

        @Test
        @DisplayName("기본 validateAndDelete - 올바른 값으로 검증 성공 및 삭제")
        void validateAndDelete_DefaultImpl_CorrectValue_ReturnsTrue() {
            // given
            testStorage.save(TEST_EMAIL, TEST_CODE);

            // when
            boolean result = testStorage.validateAndDelete(TEST_EMAIL, TEST_CODE);

            // then
            assertThat(result).isTrue();
            assertThat(testStorage.find(TEST_EMAIL)).isEmpty(); // 삭제됨
        }

        @Test
        @DisplayName("기본 validateAndDelete - 잘못된 값으로 검증 실패 및 삭제하지 않음")
        void validateAndDelete_DefaultImpl_WrongValue_ReturnsFalse() {
            // given
            testStorage.save(TEST_EMAIL, TEST_CODE);

            // when
            boolean result = testStorage.validateAndDelete(TEST_EMAIL, WRONG_CODE);

            // then
            assertThat(result).isFalse();
            assertThat(testStorage.find(TEST_EMAIL)).isPresent(); // 삭제되지 않음
        }

        @Test
        @DisplayName("기본 validateAndDelete - 존재하지 않는 키로 검증 실패")
        void validateAndDelete_DefaultImpl_NonExistingKey_ReturnsFalse() {
            // when
            boolean result = testStorage.validateAndDelete("nonexisting@email.com", TEST_CODE);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        private TestVerificationStorage testStorage;

        @BeforeEach
        void setUp() {
            testStorage = new TestVerificationStorage();
        }

        @Test
        @DisplayName("완전한 인증 플로우 - 저장, 조회, 검증, 삭제")
        void integrationScenario_CompleteVerificationFlow() {
            String email = "integration@test.com";
            String code = "654321";

            // 1. 인증 코드 저장
            testStorage.save(email, code);

            // 2. 저장된 코드 조회 확인
            Optional<String> savedCode = testStorage.find(email);
            assertThat(savedCode).isPresent();
            assertThat(savedCode.get()).isEqualTo(code);

            // 3. 올바른 코드로 검증 및 삭제
            boolean validationResult = testStorage.validateAndDelete(email, code);
            assertThat(validationResult).isTrue();

            // 4. 삭제 후 조회 시 빈 결과
            Optional<String> afterDeletion = testStorage.find(email);
            assertThat(afterDeletion).isEmpty();
        }

        @Test
        @DisplayName("잘못된 인증 시나리오 - 코드 불일치")
        void integrationScenario_WrongCodeFlow() {
            String email = "wrong@test.com";
            String correctCode = "111111";
            String wrongCode = "999999";

            // 1. 인증 코드 저장
            testStorage.save(email, correctCode);

            // 2. 잘못된 코드로 검증 시도
            boolean validationResult = testStorage.validateAndDelete(email, wrongCode);
            assertThat(validationResult).isFalse();

            // 3. 코드가 여전히 존재하는지 확인 (삭제되지 않음)
            Optional<String> stillExists = testStorage.find(email);
            assertThat(stillExists).isPresent();
            assertThat(stillExists.get()).isEqualTo(correctCode);

            // 4. 올바른 코드로 다시 검증
            boolean secondValidation = testStorage.validateAndDelete(email, correctCode);
            assertThat(secondValidation).isTrue();

            // 5. 이제 삭제됨
            assertThat(testStorage.find(email)).isEmpty();
        }

        // 테스트용 간단한 구현체
        private static class TestVerificationStorage implements VerificationStorage {
            private String storedKey;
            private String storedValue;

            @Override
            public void save(String key, String value) {
                this.storedKey = key;
                this.storedValue = value;
            }

            @Override
            public Optional<String> find(String key) {
                if (key.equals(storedKey)) {
                    return Optional.ofNullable(storedValue);
                }
                return Optional.empty();
            }

            @Override
            public void delete(String key) {
                if (key.equals(storedKey)) {
                    this.storedKey = null;
                    this.storedValue = null;
                }
            }
        }
    }
}
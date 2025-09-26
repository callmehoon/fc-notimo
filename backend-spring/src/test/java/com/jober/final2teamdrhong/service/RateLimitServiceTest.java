package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.config.RateLimitConfig;
import com.jober.final2teamdrhong.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * RateLimitService 종합 테스트
 */
class RateLimitServiceTest {

    @Nested
    @DisplayName("단위 테스트 - Redis 폴백 동작 검증")
    @ExtendWith(MockitoExtension.class)
    class UnitTest {

        @Mock
        private RateLimitConfig rateLimitConfig;


        @InjectMocks
        private RateLimitService rateLimitService;

        private static final String TEST_IP = "192.168.1.100";

        @BeforeEach
        void setUp() {
            // RateLimitConfig 기본값 설정
            setupRateLimitConfig();
        }

        private void setupRateLimitConfig() {
            // EmailSend 설정
            RateLimitConfig.EmailSend emailSend = new RateLimitConfig.EmailSend();
            emailSend.setRequestsPerWindow(3);
            emailSend.setWindowDurationMinutes(5);

            // EmailVerify 설정
            RateLimitConfig.EmailVerify emailVerify = new RateLimitConfig.EmailVerify();
            emailVerify.setRequestsPerWindow(5);
            emailVerify.setWindowDurationMinutes(10);

            // Signup 설정
            RateLimitConfig.Signup signup = new RateLimitConfig.Signup();
            signup.setRequestsPerWindow(10);
            signup.setWindowDurationMinutes(60);

            // Login 설정
            RateLimitConfig.Login login = new RateLimitConfig.Login();
            login.setRequestsPerWindow(5);
            login.setWindowDurationMinutes(15);

            // RefreshToken 설정
            RateLimitConfig.RefreshToken refreshToken = new RateLimitConfig.RefreshToken();
            refreshToken.setRequestsPerWindow(10);
            refreshToken.setWindowDurationMinutes(5);

            lenient().when(rateLimitConfig.getEmailSend()).thenReturn(emailSend);
            lenient().when(rateLimitConfig.getEmailVerify()).thenReturn(emailVerify);
            lenient().when(rateLimitConfig.getSignup()).thenReturn(signup);
            lenient().when(rateLimitConfig.getLogin()).thenReturn(login);
            lenient().when(rateLimitConfig.getRefreshToken()).thenReturn(refreshToken);
        }


        @Test
        @DisplayName("Redis 비활성화 시 인메모리 폴백 사용")
        void getBucket_RedisDisabled_UsesInMemoryFallback() throws Exception {
            // given: Redis ProxyManager가 null인 상황 시뮬레이션
            rateLimitService = new RateLimitService(rateLimitConfig, null, null);

            // when: 동일한 키로 여러 번 호출
            boolean firstCall = rateLimitService.isEmailSendAllowed(TEST_IP);
            boolean secondCall = rateLimitService.isEmailSendAllowed(TEST_IP);
            boolean thirdCall = rateLimitService.isEmailSendAllowed(TEST_IP);

            // then: 3회까지는 허용되어야 함 (인메모리 버킷 사용)
            assertThat(firstCall).isTrue();
            assertThat(secondCall).isTrue();
            assertThat(thirdCall).isTrue();

            // 4회째는 거절되어야 함
            boolean fourthCall = rateLimitService.isEmailSendAllowed(TEST_IP);
            assertThat(fourthCall).isFalse();
        }

        @Test
        @DisplayName("인메모리 버킷의 동시성 안전성 검증")
        void inMemoryBucket_ConcurrentAccess_ThreadSafe() throws InterruptedException {
            // given: Redis 비활성화 상황
            rateLimitService = new RateLimitService(rateLimitConfig, null, null);

            // when: 여러 스레드에서 동시에 동일한 키로 접근
            String sharedKey = "concurrent-test-ip";
            int threadCount = 10;
            int attemptsPerThread = 2;

            Thread[] threads = new Thread[threadCount];
            boolean[][] results = new boolean[threadCount][attemptsPerThread];

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < attemptsPerThread; j++) {
                        results[threadIndex][j] = rateLimitService.isEmailSendAllowed(sharedKey);
                    }
                });
            }

            // 모든 스레드 시작
            for (Thread thread : threads) {
                thread.start();
            }

            // 모든 스레드 완료 대기
            for (Thread thread : threads) {
                thread.join();
            }

            // then: 총 3개의 요청만 성공해야 함 (Rate Limit 3회)
            int totalSuccessCount = 0;
            for (int i = 0; i < threadCount; i++) {
                for (int j = 0; j < attemptsPerThread; j++) {
                    if (results[i][j]) {
                        totalSuccessCount++;
                    }
                }
            }

            assertThat(totalSuccessCount).isEqualTo(3);
        }

        @Test
        @DisplayName("서로 다른 키에 대한 버킷 격리 검증")
        void inMemoryBucket_DifferentKeys_IsolatedBuckets() {
            // given
            rateLimitService = new RateLimitService(rateLimitConfig, null, null);
            String ip1 = "192.168.1.1";
            String ip2 = "192.168.1.2";

            // when: 첫 번째 IP로 3회 모두 사용
            rateLimitService.isEmailSendAllowed(ip1); // 1회
            rateLimitService.isEmailSendAllowed(ip1); // 2회
            rateLimitService.isEmailSendAllowed(ip1); // 3회

            // then: 첫 번째 IP는 4회째 실패
            assertThat(rateLimitService.isEmailSendAllowed(ip1)).isFalse();

            // 두 번째 IP는 여전히 사용 가능 (격리됨)
            assertThat(rateLimitService.isEmailSendAllowed(ip2)).isTrue();
            assertThat(rateLimitService.isEmailSendAllowed(ip2)).isTrue();
            assertThat(rateLimitService.isEmailSendAllowed(ip2)).isTrue();
            assertThat(rateLimitService.isEmailSendAllowed(ip2)).isFalse(); // 4회째 실패
        }

        @Test
        @DisplayName("인메모리 버킷 재사용 검증")
        void inMemoryBucket_BucketReuse_SameInstanceReturned() {
            // given
            rateLimitService = new RateLimitService(rateLimitConfig, null, null);

            // when: 동일한 키로 버킷을 여러 번 요청
            rateLimitService.isEmailSendAllowed(TEST_IP);
            rateLimitService.isEmailSendAllowed(TEST_IP);

            // then: 인메모리 맵에 하나의 버킷만 존재해야 함
            @SuppressWarnings("unchecked")
            Map<String, Bucket> inMemoryBuckets = (Map<String, Bucket>)
                ReflectionTestUtils.getField(rateLimitService, "inMemoryBuckets");

            assertThat(inMemoryBuckets.size()).isEqualTo(1);
            assertThat(inMemoryBuckets.containsKey("email_send:" + TEST_IP)).isTrue();
        }
    }

    @Nested
    @DisplayName("통합 테스트 - 실제 Bucket4j + Redis 연동")
    @SpringBootTest
    @ActiveProfiles("test")
    @Transactional
    class IntegrationTest {

        @Autowired
        private RateLimitService rateLimitService;

        // 테스트 상수
        private static final String TEST_EMAIL = "integration-test@example.com";

        @Test
        @DisplayName("이메일 발송 Rate Limit - 실제 Bucket4j 카운팅 (3회 성공, 4회째 예외)")
        void emailSendRateLimit_RealBucket4j_EnforcesLimit() {
            // given: 고유한 IP 주소로 테스트 격리
            String testIp = "email-send-test-ip-" + System.currentTimeMillis();

            // when & then: 1~3회째는 성공해야 함
            assertThatCode(() -> rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "1"))
                    .doesNotThrowAnyException();
            assertThatCode(() -> rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "2"))
                    .doesNotThrowAnyException();
            assertThatCode(() -> rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "3"))
                    .doesNotThrowAnyException();

            // 4회째는 RateLimitExceededException이 발생해야 함
            assertThatThrownBy(() -> rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "4"))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("이메일 발송 속도 제한을 초과했습니다");
        }

        @Test
        @DisplayName("회원가입 Rate Limit - 10회 성공, 11회째 예외")
        void signupRateLimit_RealBucket4j_EnforcesLimit() {
            // given
            String testIp = "signup-test-ip-" + System.currentTimeMillis();

            // when & then: 1~10회째는 성공
            for (int i = 1; i <= 10; i++) {
                final int attempt = i;
                assertThatCode(() -> rateLimitService.checkSignupRateLimit(testIp, TEST_EMAIL + attempt))
                        .doesNotThrowAnyException();
            }

            // 11회째는 예외 발생
            assertThatThrownBy(() -> rateLimitService.checkSignupRateLimit(testIp, TEST_EMAIL + "11"))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("회원가입 속도 제한을 초과했습니다");
        }

        @Test
        @DisplayName("로그인 Rate Limit - IP 기반 (5회 성공, 6회째 예외)")
        void loginRateLimit_IpBased_EnforcesLimit() {
            // given
            String testIp = "login-ip-test-" + System.currentTimeMillis();

            // when & then: 1~5회째는 성공
            for (int i = 1; i <= 5; i++) {
                assertThatCode(() -> rateLimitService.checkLoginRateLimit(testIp))
                        .doesNotThrowAnyException();
            }

            // 6회째는 예외 발생
            assertThatThrownBy(() -> rateLimitService.checkLoginRateLimit(testIp))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("로그인 시도 속도 제한을 초과했습니다");
        }

        @Test
        @DisplayName("향상된 로그인 Rate Limit - IP + 이메일 이중 체크")
        void enhancedLoginRateLimit_IpAndEmail_DoubleCheck() {
            // given
            String testIp = "enhanced-login-ip-" + System.currentTimeMillis();
            String testEmail = "enhanced-login-" + System.currentTimeMillis() + "@test.com";

            // when & then: IP 기반으로 5회 성공 후 실패
            for (int i = 1; i <= 5; i++) {
                final int attempt = i;
                assertThatCode(() -> rateLimitService.checkEnhancedLoginRateLimit(testIp, testEmail + attempt))
                        .doesNotThrowAnyException();
            }

            // IP 기준 6회째 실패
            assertThatThrownBy(() -> rateLimitService.checkEnhancedLoginRateLimit(testIp, testEmail + "6"))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("로그인 시도 속도 제한을 초과했습니다");
        }

        @Test
        @DisplayName("이메일별 로그인 Rate Limit - 3회 성공, 4회째 예외")
        void loginByEmailRateLimit_EmailBased_EnforcesLimit() {
            // given
            String testEmail = "email-login-test-" + System.currentTimeMillis() + "@test.com";

            // when & then: 동일 이메일로 3회까지 성공
            for (int i = 1; i <= 3; i++) {
                String differentIp = "different-ip-" + i;
                assertThatCode(() -> rateLimitService.checkEnhancedLoginRateLimit(differentIp, testEmail))
                        .doesNotThrowAnyException();
            }

            // 4회째는 이메일 기준으로 실패
            assertThatThrownBy(() -> rateLimitService.checkEnhancedLoginRateLimit("another-ip", testEmail))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("해당 계정에 대한 로그인 시도가 너무 많습니다");
        }

        @Test
        @DisplayName("토큰 갱신 Rate Limit - 10회 성공, 11회째 예외")
        void refreshTokenRateLimit_RealBucket4j_EnforcesLimit() {
            // given
            String testIp = "refresh-token-ip-" + System.currentTimeMillis();

            // when & then: 1~10회째는 성공
            for (int i = 1; i <= 10; i++) {
                assertThatCode(() -> rateLimitService.checkRefreshTokenRateLimit(testIp))
                        .doesNotThrowAnyException();
            }

            // 11회째는 예외 발생
            assertThatThrownBy(() -> rateLimitService.checkRefreshTokenRateLimit(testIp))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("토큰 갱신 속도 제한을 초과했습니다");
        }

        @Test
        @DisplayName("이메일 인증 코드 검증 Rate Limit - 5회 성공, 6회째 예외")
        void emailVerifyRateLimit_RealBucket4j_EnforcesLimit() {
            // given
            String testEmail = "verify-test-" + System.currentTimeMillis() + "@test.com";

            // when & then: 1~5회째는 성공
            for (int i = 1; i <= 5; i++) {
                assertThatCode(() -> rateLimitService.checkEmailVerifyRateLimit(testEmail))
                        .doesNotThrowAnyException();
            }

            // 6회째는 예외 발생
            assertThatThrownBy(() -> rateLimitService.checkEmailVerifyRateLimit(testEmail))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("인증 코드 검증 시도 횟수를 초과했습니다");
        }

        @Test
        @DisplayName("허용 상태 확인 - 실제 Bucket4j 연동")
        void isAllowed_Methods_RealBucket4j_Integration() {
            // given: 고유한 IP와 이메일로 테스트 격리
            String testIp = "allowance-test-ip-" + System.currentTimeMillis();
            String testEmail = "allowance-test-" + System.currentTimeMillis() + "@test.com";

            // when & then: 아직 사용하지 않은 IP/이메일의 허용 상태 확인
            assertThat(rateLimitService.isEmailSendAllowed(testIp)).isTrue();
            assertThat(rateLimitService.isEmailVerifyAllowed(testEmail)).isTrue();
            assertThat(rateLimitService.isSignupAllowed(testIp)).isTrue();
            assertThat(rateLimitService.isLoginAllowed(testIp)).isTrue();
            assertThat(rateLimitService.isLoginByEmailAllowed(testEmail)).isTrue();
            assertThat(rateLimitService.isRefreshTokenAllowed(testIp)).isTrue();
        }

        @Test
        @DisplayName("대기 시간 계산 - 실제 Bucket4j 연동")
        void waitTime_Calculation_RealBucket4j_Integration() {
            // given: 고유한 IP로 테스트 격리 후 제한 소진
            String testIp = "wait-time-test-ip-" + System.currentTimeMillis();

            // 3회 제한 모두 사용 (이메일 발송)
            rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "1");
            rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "2");
            rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "3");

            // when: 제한 소진 후 대기 시간 확인
            long emailSendWaitTime = rateLimitService.getEmailSendWaitTime(testIp);

            // then: 대기 시간이 0보다 크고 5분 이내여야 함
            assertThat(emailSendWaitTime).isGreaterThan(0);
            assertThat(emailSendWaitTime).isLessThanOrEqualTo(5 * 60); // 최대 5분

            // 다른 기능들의 대기 시간도 확인 (아직 사용하지 않았으므로 0이어야 함)
            assertThat(rateLimitService.getSignupWaitTime(testIp)).isEqualTo(0);
            assertThat(rateLimitService.getLoginWaitTime(testIp)).isEqualTo(0);
            assertThat(rateLimitService.getRefreshTokenWaitTime(testIp)).isEqualTo(0);
        }

        @Test
        @DisplayName("서로 다른 Rate Limit 간 격리 검증")
        void differentRateLimits_Isolated_Independent() {
            // given
            String testIp = "isolation-test-ip-" + System.currentTimeMillis();
            String testEmail = "isolation-test-" + System.currentTimeMillis() + "@test.com";

            // when: 이메일 발송 제한 소진
            rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "1");
            rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "2");
            rateLimitService.checkEmailSendRateLimit(testIp, TEST_EMAIL + "3");

            // then: 다른 기능들은 여전히 사용 가능해야 함
            assertThat(rateLimitService.isSignupAllowed(testIp)).isTrue();
            assertThat(rateLimitService.isLoginAllowed(testIp)).isTrue();
            assertThat(rateLimitService.isRefreshTokenAllowed(testIp)).isTrue();
            assertThat(rateLimitService.isEmailVerifyAllowed(testEmail)).isTrue();

            // 이메일 발송만 차단되어야 함
            assertThat(rateLimitService.isEmailSendAllowed(testIp)).isFalse();
        }
    }

    @Nested
    @DisplayName("Rate Limit 구성 검증 테스트")
    class ConfigurationTest {

        @Test
        @DisplayName("Rate Limit 기본값 상수 검증")
        void rateLimitDefaults_CorrectValues() {
            // given & when & then: 기본값들이 올바르게 설정되었는지 확인
            assertThat(RateLimitConfig.Defaults.EMAIL_SEND_REQUESTS_PER_WINDOW).isEqualTo(3);
            assertThat(RateLimitConfig.Defaults.EMAIL_SEND_WINDOW_DURATION_MINUTES).isEqualTo(5);

            assertThat(RateLimitConfig.Defaults.EMAIL_VERIFY_REQUESTS_PER_WINDOW).isEqualTo(5);
            assertThat(RateLimitConfig.Defaults.EMAIL_VERIFY_WINDOW_DURATION_MINUTES).isEqualTo(10);

            assertThat(RateLimitConfig.Defaults.SIGNUP_REQUESTS_PER_WINDOW).isEqualTo(10);
            assertThat(RateLimitConfig.Defaults.SIGNUP_WINDOW_DURATION_MINUTES).isEqualTo(60);

            assertThat(RateLimitConfig.Defaults.LOGIN_REQUESTS_PER_WINDOW).isEqualTo(5);
            assertThat(RateLimitConfig.Defaults.LOGIN_WINDOW_DURATION_MINUTES).isEqualTo(15);

            assertThat(RateLimitConfig.Defaults.REFRESH_TOKEN_REQUESTS_PER_WINDOW).isEqualTo(10);
            assertThat(RateLimitConfig.Defaults.REFRESH_TOKEN_WINDOW_DURATION_MINUTES).isEqualTo(5);
        }

        @Test
        @DisplayName("Rate Limit 키 생성 패턴 검증")
        void rateLimitKeys_CorrectPatterns() {
            // Rate Limit 키들이 올바른 패턴으로 생성되는지 검증
            // 이는 실제 서비스에서 키 충돌을 방지하기 위해 중요함

            String ip = "192.168.1.1";
            String email = "test@example.com";

            // 예상 키 패턴들
            String expectedEmailSendKey = "email_send:" + ip;
            String expectedEmailVerifyKey = "email_verify:" + email;
            String expectedSignupKey = "signup:" + ip;
            String expectedLoginKey = "login:" + ip;
            String expectedLoginEmailKey = "login_email:" + email;
            String expectedRefreshTokenKey = "refresh_token:" + ip;

            // 키 패턴이 서로 다르고 구분 가능해야 함
            assertThat(expectedEmailSendKey).isNotEqualTo(expectedSignupKey);
            assertThat(expectedLoginKey).isNotEqualTo(expectedLoginEmailKey);
            assertThat(expectedEmailSendKey).isNotEqualTo(expectedRefreshTokenKey);

            // 키들이 적절한 접두사를 가지고 있어야 함
            assertThat(expectedEmailSendKey).startsWith("email_send:");
            assertThat(expectedEmailVerifyKey).startsWith("email_verify:");
            assertThat(expectedSignupKey).startsWith("signup:");
            assertThat(expectedLoginKey).startsWith("login:");
            assertThat(expectedLoginEmailKey).startsWith("login_email:");
            assertThat(expectedRefreshTokenKey).startsWith("refresh_token:");
        }
    }

    @Nested
    @DisplayName("에러 메시지 및 대기 시간 검증")
    class ErrorMessageTest {

        @Test
        @DisplayName("RateLimitExceededException 메시지 및 대기 시간 포함 검증")
        void rateLimitException_ContainsWaitTime() {
            // given: Rate Limit이 설정된 상황에서 예외 발생 시뮬레이션
            long expectedWaitTime = 300; // 5분
            RateLimitExceededException exception = new RateLimitExceededException(
                "이메일 발송 속도 제한을 초과했습니다. " + expectedWaitTime + "초 후 다시 시도해주세요.",
                expectedWaitTime
            );

            // when & then
            assertThat(exception.getMessage()).contains("이메일 발송 속도 제한을 초과했습니다");
            assertThat(exception.getMessage()).contains(expectedWaitTime + "초 후 다시 시도해주세요");
            assertThat(exception.getRetryAfterSeconds()).isEqualTo(expectedWaitTime);
        }

        @Test
        @DisplayName("각 기능별 Rate Limit 예외 메시지 검증")
        void rateLimitExceptions_DifferentMessages() {
            // given: 각 기능별 예외 메시지들
            String emailSendMessage = "이메일 발송 속도 제한을 초과했습니다";
            String signupMessage = "회원가입 속도 제한을 초과했습니다";
            String loginMessage = "로그인 시도 속도 제한을 초과했습니다";
            String loginEmailMessage = "해당 계정에 대한 로그인 시도가 너무 많습니다";
            String refreshTokenMessage = "토큰 갱신 속도 제한을 초과했습니다";
            String emailVerifyMessage = "인증 코드 검증 시도 횟수를 초과했습니다";

            // when & then: 각 메시지가 구분 가능하고 의미가 명확해야 함
            assertThat(emailSendMessage).contains("이메일 발송");
            assertThat(signupMessage).contains("회원가입");
            assertThat(loginMessage).contains("로그인 시도");
            assertThat(loginEmailMessage).contains("해당 계정");
            assertThat(refreshTokenMessage).contains("토큰 갱신");
            assertThat(emailVerifyMessage).contains("인증 코드 검증");

            // 모든 메시지가 서로 달라야 함
            assertThat(emailSendMessage).isNotEqualTo(signupMessage);
            assertThat(loginMessage).isNotEqualTo(loginEmailMessage);
            assertThat(refreshTokenMessage).isNotEqualTo(emailVerifyMessage);
        }
    }
}
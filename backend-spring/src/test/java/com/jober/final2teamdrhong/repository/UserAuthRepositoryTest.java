package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserAuthRepositoryTest {

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private UserAuth testUserAuth;

    @BeforeEach
    void setUp() {
        // 1. 테스트용 사용자를 생성하고 저장합니다.
        testUser = User.create("테스트사용자", "test@example.com", "010-1234-5678");
        entityManager.persist(testUser);

        // 2. 사용자에 대한 로컬 인증 정보를 생성합니다.
        testUserAuth = UserAuth.createLocalAuth(testUser, "$2a$10$encoded.password.hash");
        entityManager.persist(testUserAuth);
        testUser.addUserAuth(testUserAuth);

        // 3. 변경사항을 DB에 반영하고 영속성 컨텍스트를 비웁니다.
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("사용자 인증 정보 저장 성공 테스트")
    void save_Success_Test() {
        // given
        // 1. 새로운 사용자를 생성하고 저장합니다.
        User newUser = User.create("새사용자", "newuser@example.com", "010-9876-5432");
        entityManager.persist(newUser);
        entityManager.flush();

        // 2. 새로운 사용자 인증 정보를 생성합니다.
        UserAuth newUserAuth = UserAuth.createLocalAuth(newUser, "$2a$10$new.encoded.password.hash");

        // when
        // 1. 사용자 인증 정보를 저장합니다.
        UserAuth savedUserAuth = userAuthRepository.save(newUserAuth);

        // then
        // 1. 저장된 인증 정보에 ID가 할당되었는지 확인합니다.
        assertThat(savedUserAuth.getAuthId()).isNotNull();
        // 2. 저장된 인증 정보의 내용이 정확한지 검증합니다.
        assertThat(savedUserAuth.getAuthType()).isEqualTo(UserAuth.AuthType.LOCAL);
        assertThat(savedUserAuth.getPasswordHash()).isEqualTo("$2a$10$new.encoded.password.hash");
        assertThat(savedUserAuth.getUser()).isEqualTo(newUser);

        // 3. 실제로 DB에 저장되었는지 다시 조회하여 확인합니다.
        Optional<UserAuth> foundUserAuth = userAuthRepository.findById(savedUserAuth.getAuthId());
        assertThat(foundUserAuth).isPresent();
        assertThat(foundUserAuth.get().getPasswordHash()).isEqualTo("$2a$10$new.encoded.password.hash");
    }

    @Test
    @DisplayName("ID로 사용자 인증 정보 조회 성공 테스트")
    void findById_Success_Test() {
        // given
        // 1. 조회할 인증 정보 ID를 준비합니다.
        Integer authId = testUserAuth.getAuthId();

        // when
        // 1. ID로 사용자 인증 정보를 조회합니다.
        Optional<UserAuth> result = userAuthRepository.findById(authId);

        // then
        // 1. 조회 결과가 존재하는지 확인합니다.
        assertThat(result).isPresent();
        // 2. 조회된 인증 정보의 내용이 정확한지 검증합니다.
        UserAuth foundAuth = result.get();
        assertThat(foundAuth.getAuthType()).isEqualTo(UserAuth.AuthType.LOCAL);
        assertThat(foundAuth.getPasswordHash()).isEqualTo("$2a$10$encoded.password.hash");
        assertThat(foundAuth.getUser().getUserEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("ID로 사용자 인증 정보 조회 실패 테스트 - 존재하지 않는 ID")
    void findById_Fail_IdNotFound_Test() {
        // given
        // 1. 존재하지 않는 인증 정보 ID를 준비합니다.
        Integer nonExistingId = 99999;

        // when
        // 1. 존재하지 않는 ID로 사용자 인증 정보를 조회합니다.
        Optional<UserAuth> result = userAuthRepository.findById(nonExistingId);

        // then
        // 1. 조회 결과가 비어있는지 확인합니다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 인증 정보 삭제 성공 테스트")
    void deleteById_Success_Test() {
        // given
        // 1. 삭제할 인증 정보 ID를 준비합니다.
        Integer authIdToDelete = testUserAuth.getAuthId();

        // 2. 삭제 전에 인증 정보가 존재하는지 확인합니다.
        assertThat(userAuthRepository.findById(authIdToDelete)).isPresent();

        // when
        // 1. 사용자 인증 정보를 삭제합니다.
        userAuthRepository.deleteById(authIdToDelete);
        entityManager.flush(); // 삭제를 즉시 DB에 반영

        // then
        // 1. 인증 정보가 삭제되었는지 확인합니다.
        Optional<UserAuth> deletedAuth = userAuthRepository.findById(authIdToDelete);
        assertThat(deletedAuth).isEmpty();
    }

    @Test
    @DisplayName("소셜 인증 정보 저장 성공 테스트")
    void save_SocialAuth_Success_Test() {
        // given
        // 1. 소셜 로그인용 사용자를 생성하고 저장합니다.
        User socialUser = User.create("소셜사용자", "social@example.com", "010-1111-2222");
        entityManager.persist(socialUser);
        entityManager.flush();

        // 2. 구글 소셜 인증 정보를 생성합니다.
        UserAuth googleAuth = UserAuth.createSocialAuth(socialUser, UserAuth.AuthType.GOOGLE, "google_123");

        // when
        // 1. 소셜 인증 정보를 저장합니다.
        UserAuth savedAuth = userAuthRepository.save(googleAuth);

        // then
        // 1. 저장된 인증 정보에 ID가 할당되었는지 확인합니다.
        assertThat(savedAuth.getAuthId()).isNotNull();
        // 2. 저장된 소셜 인증 정보의 내용이 정확한지 검증합니다.
        assertThat(savedAuth.getAuthType()).isEqualTo(UserAuth.AuthType.GOOGLE);
        assertThat(savedAuth.getSocialId()).isEqualTo("google_123");
        assertThat(savedAuth.getPasswordHash()).isNull(); // 소셜 로그인은 패스워드 없음
        assertThat(savedAuth.getUser()).isEqualTo(socialUser);
    }
}
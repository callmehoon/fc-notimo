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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 1. 테스트용 사용자를 생성하고 저장합니다.
        testUser = User.create("테스트사용자", "test@example.com", "010-1234-5678");
        entityManager.persist(testUser);

        // 2. 사용자에 대한 로컬 인증 정보를 생성합니다.
        UserAuth userAuth = UserAuth.createLocalAuth(testUser, "$2a$10$encoded.password.hash");
        entityManager.persist(userAuth);
        testUser.addUserAuth(userAuth);

        // 3. 변경사항을 DB에 반영하고 영속성 컨텍스트를 비웁니다.
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공 테스트")
    void findByUserEmail_Success_Test() {
        // given
        // 1. 테스트에 사용할 이메일을 준비합니다.
        String testEmail = "test@example.com";

        // when
        // 1. 이메일로 사용자를 조회합니다.
        Optional<User> result = userRepository.findByUserEmail(testEmail);

        // then
        // 1. 조회 결과가 존재하는지 확인합니다.
        assertThat(result).isPresent();
        // 2. 조회된 사용자의 정보가 정확한지 검증합니다.
        assertThat(result.get().getUserEmail()).isEqualTo(testEmail);
        assertThat(result.get().getUserName()).isEqualTo("테스트사용자");
        assertThat(result.get().getUserNumber()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("이메일로 사용자 조회 실패 테스트 - 존재하지 않는 이메일")
    void findByUserEmail_Fail_EmailNotFound_Test() {
        // given
        // 1. 존재하지 않는 이메일을 준비합니다.
        String nonExistingEmail = "nonexisting@example.com";

        // when
        // 1. 존재하지 않는 이메일로 사용자를 조회합니다.
        Optional<User> result = userRepository.findByUserEmail(nonExistingEmail);

        // then
        // 1. 조회 결과가 비어있는지 확인합니다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일로 사용자와 인증정보 함께 조회 성공 테스트")
    void findByUserEmailWithAuth_Success_Test() {
        // given
        // 1. 테스트에 사용할 이메일을 준비합니다.
        String testEmail = "test@example.com";

        // when
        // 1. 이메일로 사용자와 인증정보를 함께 조회합니다.
        Optional<User> result = userRepository.findByUserEmailWithAuth(testEmail);

        // then
        // 1. 조회 결과가 존재하는지 확인합니다.
        assertThat(result).isPresent();
        User foundUser = result.get();

        // 2. 사용자 정보가 정확한지 검증합니다.
        assertThat(foundUser.getUserEmail()).isEqualTo(testEmail);
        assertThat(foundUser.getUserName()).isEqualTo("테스트사용자");

        // 3. UserAuth 정보가 함께 로드되었는지 검증합니다 (N+1 쿼리 방지 확인)
        assertThat(foundUser.getUserAuths()).isNotEmpty();
        assertThat(foundUser.getUserAuths()).hasSize(1);

        // 4. 로드된 UserAuth 정보가 정확한지 검증합니다.
        UserAuth loadedAuth = foundUser.getUserAuths().iterator().next();
        assertThat(loadedAuth.getAuthType()).isEqualTo(UserAuth.AuthType.LOCAL);
        assertThat(loadedAuth.getPasswordHash()).isEqualTo("$2a$10$encoded.password.hash");
    }

    @Test
    @DisplayName("사용자 저장 성공 테스트")
    void save_Success_Test() {
        // given
        // 1. 새로운 사용자를 생성합니다.
        User newUser = User.create("새사용자", "newuser@example.com", "010-9876-5432");

        // when
        // 1. 사용자를 저장합니다.
        User savedUser = userRepository.save(newUser);

        // then
        // 1. 저장된 사용자에 ID가 할당되었는지 확인합니다.
        assertThat(savedUser.getUserId()).isNotNull();
        // 2. 저장된 사용자의 정보가 정확한지 검증합니다.
        assertThat(savedUser.getUserName()).isEqualTo("새사용자");
        assertThat(savedUser.getUserEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getUserNumber()).isEqualTo("010-9876-5432");

        // 3. 실제로 DB에 저장되었는지 다시 조회하여 확인합니다.
        Optional<User> foundUser = userRepository.findById(savedUser.getUserId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("사용자 삭제 성공 테스트")
    void deleteById_Success_Test() {
        // given
        // 1. 삭제할 사용자의 ID를 준비합니다.
        Integer userIdToDelete = testUser.getUserId();

        // 2. 삭제 전에 사용자가 존재하는지 확인합니다.
        assertThat(userRepository.findById(userIdToDelete)).isPresent();

        // when
        // 1. 사용자를 삭제합니다.
        userRepository.deleteById(userIdToDelete);
        entityManager.flush(); // 삭제를 즉시 DB에 반영

        // then
        // 1. 사용자가 삭제되었는지 확인합니다.
        Optional<User> deletedUser = userRepository.findById(userIdToDelete);
        assertThat(deletedUser).isEmpty();
    }
}
package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("사용자 ID로 조회 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("사용자 ID로 조회 성공 테스트")
        void shouldReturnUserWhenValidUserId() {
            // given
            // 1. 테스트용 사용자 ID를 준비합니다.
            Integer userId = 1;
            // 2. 모의 사용자 객체를 생성합니다.
            User mockUser = User.create("테스트사용자", "test@example.com", "010-1234-5678");
            // 3. Repository가 사용자를 반환하도록 설정합니다.
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            // 1. ID로 사용자를 조회합니다.
            Optional<User> result = userService.findById(userId);

            // then
            // 1. 조회 결과가 존재하는지 확인합니다.
            assertThat(result).isPresent();
            // 2. 반환된 사용자 정보가 정확한지 검증합니다.
            assertThat(result.get().getUserName()).isEqualTo("테스트사용자");
            assertThat(result.get().getUserEmail()).isEqualTo("test@example.com");
            // 3. Repository의 findById 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID 조회 실패 테스트")
        void shouldReturnEmptyWhenUserNotFound() {
            // given
            // 1. 존재하지 않는 사용자 ID를 준비합니다.
            Integer nonExistingUserId = 999;
            // 2. Repository가 빈 Optional을 반환하도록 설정합니다.
            given(userRepository.findById(nonExistingUserId)).willReturn(Optional.empty());

            // when
            // 1. 존재하지 않는 ID로 사용자를 조회합니다.
            Optional<User> result = userService.findById(nonExistingUserId);

            // then
            // 1. 조회 결과가 비어있는지 확인합니다.
            assertThat(result).isEmpty();
            // 2. Repository의 findById 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).findById(nonExistingUserId);
        }

        @Test
        @DisplayName("null 사용자 ID 조회 실패 테스트")
        void shouldReturnEmptyWhenUserIdIsNull() {
            // given
            // 1. null 사용자 ID를 준비합니다.
            Integer nullUserId = null;
            // 2. Repository가 빈 Optional을 반환하도록 설정합니다.
            given(userRepository.findById(nullUserId)).willReturn(Optional.empty());

            // when
            // 1. null ID로 사용자를 조회합니다.
            Optional<User> result = userService.findById(nullUserId);

            // then
            // 1. 조회 결과가 비어있는지 확인합니다.
            assertThat(result).isEmpty();
            // 2. Repository의 findById 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).findById(nullUserId);
        }
    }

    @Nested
    @DisplayName("사용자 이메일로 조회 테스트")
    class FindByEmailTest {

        @Test
        @DisplayName("이메일로 사용자 조회 성공 테스트")
        void shouldReturnUserWhenValidEmail() {
            // given
            // 1. 테스트용 이메일을 준비합니다.
            String email = "test@example.com";
            // 2. 모의 사용자 객체를 생성합니다.
            User mockUser = User.create("테스트사용자", email, "010-1234-5678");
            // 3. Repository가 사용자를 반환하도록 설정합니다.
            given(userRepository.findByUserEmail(email)).willReturn(Optional.of(mockUser));

            // when
            // 1. 이메일로 사용자를 조회합니다.
            Optional<User> result = userService.findByEmail(email);

            // then
            // 1. 조회 결과가 존재하는지 확인합니다.
            assertThat(result).isPresent();
            // 2. 반환된 사용자 정보가 정확한지 검증합니다.
            assertThat(result.get().getUserEmail()).isEqualTo(email);
            assertThat(result.get().getUserName()).isEqualTo("테스트사용자");
            // 3. Repository의 findByUserEmail 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).findByUserEmail(email);
        }

        @Test
        @DisplayName("존재하지 않는 이메일 조회 실패 테스트")
        void shouldReturnEmptyWhenEmailNotFound() {
            // given
            // 1. 존재하지 않는 이메일을 준비합니다.
            String nonExistingEmail = "notfound@example.com";
            // 2. Repository가 빈 Optional을 반환하도록 설정합니다.
            given(userRepository.findByUserEmail(nonExistingEmail)).willReturn(Optional.empty());

            // when
            // 1. 존재하지 않는 이메일로 사용자를 조회합니다.
            Optional<User> result = userService.findByEmail(nonExistingEmail);

            // then
            // 1. 조회 결과가 비어있는지 확인합니다.
            assertThat(result).isEmpty();
            // 2. Repository의 findByUserEmail 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).findByUserEmail(nonExistingEmail);
        }

        @Test
        @DisplayName("빈 이메일 조회 실패 테스트")
        void shouldReturnEmptyWhenEmailIsEmpty() {
            // given
            // 1. 빈 이메일을 준비합니다.
            String emptyEmail = "";
            // 2. Repository가 빈 Optional을 반환하도록 설정합니다.
            given(userRepository.findByUserEmail(emptyEmail)).willReturn(Optional.empty());

            // when
            // 1. 빈 이메일로 사용자를 조회합니다.
            Optional<User> result = userService.findByEmail(emptyEmail);

            // then
            // 1. 조회 결과가 비어있는지 확인합니다.
            assertThat(result).isEmpty();
            // 2. Repository의 findByUserEmail 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).findByUserEmail(emptyEmail);
        }
    }

    @Nested
    @DisplayName("사용자 저장 테스트")
    class SaveUserTest {

        @Test
        @DisplayName("사용자 저장 성공 테스트")
        void shouldSaveUserSuccessfully() {
            // given
            // 1. 저장할 사용자 객체를 생성합니다.
            User userToSave = User.create("새사용자", "newuser@example.com", "010-9876-5432");
            // 2. Repository가 저장된 사용자를 반환하도록 설정합니다.
            given(userRepository.save(userToSave)).willReturn(userToSave);

            // when
            // 1. 사용자를 저장합니다.
            User result = userService.saveUser(userToSave);

            // then
            // 1. 저장 결과가 올바른지 확인합니다.
            assertThat(result).isNotNull();
            assertThat(result.getUserName()).isEqualTo("새사용자");
            assertThat(result.getUserEmail()).isEqualTo("newuser@example.com");
            // 2. Repository의 save 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).save(userToSave);
        }

        @Test
        @DisplayName("소셜 사용자 저장 성공 테스트")
        void shouldSaveSocialUserSuccessfully() {
            // given
            // 1. 소셜 로그인 사용자 객체를 생성합니다.
            User socialUser = User.create("소셜사용자", "social@example.com", "010-1111-2222");
            // 2. Repository가 저장된 사용자를 반환하도록 설정합니다.
            given(userRepository.save(socialUser)).willReturn(socialUser);

            // when
            // 1. 소셜 사용자를 저장합니다.
            User result = userService.saveUser(socialUser);

            // then
            // 1. 저장 결과가 올바른지 확인합니다.
            assertThat(result).isNotNull();
            assertThat(result.getUserName()).isEqualTo("소셜사용자");
            assertThat(result.getUserEmail()).isEqualTo("social@example.com");
            // 2. Repository의 save 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).save(socialUser);
        }
    }

    @Nested
    @DisplayName("사용자 삭제 테스트")
    class DeleteUserTest {

        @Test
        @DisplayName("사용자 삭제 성공 테스트")
        void shouldDeleteUserSuccessfully() {
            // given
            // 1. 삭제할 사용자 ID를 준비합니다.
            Integer userIdToDelete = 1;

            // when
            // 1. 사용자를 삭제합니다.
            userService.deleteUser(userIdToDelete);

            // then
            // 1. Repository의 deleteById 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).deleteById(userIdToDelete);
        }

        @Test
        @DisplayName("null ID 사용자 삭제 테스트")
        void shouldCallRepositoryWhenUserIdIsNull() {
            // given
            // 1. null 사용자 ID를 준비합니다.
            Integer nullUserId = null;

            // when
            // 1. null ID로 사용자 삭제를 시도합니다.
            userService.deleteUser(nullUserId);

            // then
            // 1. Repository의 deleteById 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).deleteById(nullUserId);
        }
    }

    @Nested
    @DisplayName("전체 사용자 조회 테스트")
    class FindAllUsersTest {

        @Test
        @DisplayName("전체 사용자 페이지 조회 성공 테스트")
        void shouldReturnPageOfUsersSuccessfully() {
            // given
            // 1. 페이지 요청 객체를 생성합니다.
            Pageable pageable = PageRequest.of(0, 10);
            // 2. 모의 사용자 목록을 생성합니다.
            User user1 = User.create("사용자1", "user1@example.com", "010-1111-1111");
            User user2 = User.create("사용자2", "user2@example.com", "010-2222-2222");
            Page<User> mockPage = new PageImpl<>(Arrays.asList(user1, user2), pageable, 2);
            // 3. Repository가 사용자 페이지를 반환하도록 설정합니다.
            given(userRepository.findAll(pageable)).willReturn(mockPage);

            // when
            // 1. 전체 사용자를 페이지별로 조회합니다.
            Page<User> result = userService.findAllUsers(pageable);

            // then
            // 1. 조회 결과가 올바른지 확인합니다.
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).getUserName()).isEqualTo("사용자1");
            assertThat(result.getContent().get(1).getUserName()).isEqualTo("사용자2");
            // 2. Repository의 findAll 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("빈 사용자 목록 조회 테스트")
        void shouldReturnEmptyPageWhenNoUsers() {
            // given
            // 1. 페이지 요청 객체를 생성합니다.
            Pageable pageable = PageRequest.of(0, 10);
            // 2. 빈 페이지를 생성합니다.
            Page<User> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            // 3. Repository가 빈 페이지를 반환하도록 설정합니다.
            given(userRepository.findAll(pageable)).willReturn(emptyPage);

            // when
            // 1. 전체 사용자를 페이지별로 조회합니다.
            Page<User> result = userService.findAllUsers(pageable);

            // then
            // 1. 조회 결과가 빈 페이지인지 확인합니다.
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            // 2. Repository의 findAll 메소드가 호출되었는지 검증합니다.
            then(userRepository).should(times(1)).findAll(pageable);
        }
    }
}
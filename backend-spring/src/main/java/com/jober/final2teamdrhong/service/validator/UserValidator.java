package com.jober.final2teamdrhong.service.validator;

import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    /**
     * 사용자가 존재하는지 검증하고, 존재하면 User 엔티티를 반환합니다.
     * @param userId 검증할 사용자의 ID
     * @return 검증에 성공한 User 엔티티
     * @throws IllegalArgumentException 해당 ID의 사용자가 존재하지 않을 경우
     */
    public User validateAndGetUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
    }
}

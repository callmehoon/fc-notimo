package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * 이메일로 사용자를 조회합니다. (회원가입 시 중복 체크용)
     * @param email 사용자 이메일
     * @return Optional<User>
     */
    Optional<User> findByUserEmail(String email); //  이메일 유효성 검사
}

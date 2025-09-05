package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 이메일 인증 Repository (RDB용)
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
}

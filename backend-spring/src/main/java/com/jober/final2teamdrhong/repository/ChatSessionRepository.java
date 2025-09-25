package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {
    Optional<ChatSession> findBySessionIdAndWorkspace_WorkspaceIdAndWorkspace_User_UserId(Integer sessionId, Integer workspaceId, Integer userId);
}

package com.pentastack.skillsync.domain.repository;

import com.pentastack.skillsync.domain.SessionAuditLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionAuditLogRepository extends JpaRepository<SessionAuditLog, Long> {
    Optional<SessionAuditLog> findBySession_Id(Long sessionId);
}

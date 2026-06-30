package com.pentastack.skillsync.sessions.dto;

import com.pentastack.skillsync.domain.SessionStatus;
import java.time.LocalDateTime;

public record SessionResponse(
    Long id,
    Long mentorId,
    String mentorName,
    Long studentId,
    String studentName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String description,
    SessionStatus status,
    String evaluationNotes,
    String meetingLink,
    SessionAuditLogResponse audit
) {}

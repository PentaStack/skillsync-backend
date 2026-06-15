package com.pentastack.skillsync.sessions.dto;

import com.pentastack.skillsync.domain.SessionStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UpdateSessionRequest(
    SessionStatus status,
    LocalDateTime startTime,
    @Size(max = 2000) String description,
    @Size(max = 2000) String evaluationNotes
) {}

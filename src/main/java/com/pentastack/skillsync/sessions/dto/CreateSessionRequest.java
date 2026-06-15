package com.pentastack.skillsync.sessions.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateSessionRequest(
    @NotNull Long mentorId,
    @NotNull LocalDateTime startTime,
    @Size(min = 5, max = 2000) String description
) {}

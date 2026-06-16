package com.pentastack.skillsync.sessions.dto;

import com.pentastack.skillsync.domain.AuditStatus;

public record SessionAuditLogResponse(
    Long id,
    String predictedTag,
    Double confidenceScore,
    AuditStatus status,
    String errorMessage,
    Long latencyMs
) {}

package com.pentastack.skillsync.admin.dto;

import java.util.List;

public record AdminStudentDetailResponse(
    Long id,
    String displayName,
    String email,
    long totalSessions,
    List<AdminSessionSummaryResponse> sessions
) {}

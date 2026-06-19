package com.pentastack.skillsync.admin.dto;

public record AdminStudentListResponse(
    Long id,
    String displayName,
    String email,
    long totalSessions
) {}

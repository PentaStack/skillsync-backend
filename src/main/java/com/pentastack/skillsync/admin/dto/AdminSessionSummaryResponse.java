package com.pentastack.skillsync.admin.dto;

public record AdminSessionSummaryResponse(
    Long id,
    String mentorName,
    String studentName,
    String studentEmail,
    String startTime,
    String endTime,
    String status,
    String description
) {}

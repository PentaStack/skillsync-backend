package com.pentastack.skillsync.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminMentorDetailResponse(
    Long id,
    String displayName,
    String email,
    String stackName,
    Long stackId,
    String title,
    String bio,
    boolean available,
    Double rating,
    BigDecimal hourlyRate,
    long totalSessions,
    List<AdminSessionSummaryResponse> sessions
) {}

package com.pentastack.skillsync.admin.dto;

import java.math.BigDecimal;

public record AdminMentorListResponse(
    Long id,
    String displayName,
    String email,
    String stackName,
    String title,
    String bio,
    boolean available,
    Double rating,
    BigDecimal hourlyRate,
    long totalSessions
) {}

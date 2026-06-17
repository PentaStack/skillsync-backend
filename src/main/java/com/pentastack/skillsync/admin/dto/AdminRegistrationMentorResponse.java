package com.pentastack.skillsync.admin.dto;

import java.time.LocalDateTime;

public record AdminRegistrationMentorResponse(
    Long id,
    String name,
    String email,
    String stackName,
    LocalDateTime appliedDate,
    boolean isVerified
) {}

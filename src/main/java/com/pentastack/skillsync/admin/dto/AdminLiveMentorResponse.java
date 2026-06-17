package com.pentastack.skillsync.admin.dto;

public record AdminLiveMentorResponse(
    Long id,
    String displayName,
    String email,
    String stackName,
    boolean available,
    Double rating
) {}

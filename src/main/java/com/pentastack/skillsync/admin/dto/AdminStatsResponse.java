package com.pentastack.skillsync.admin.dto;

public record AdminStatsResponse(
    long totalSessions,
    long activeMentors,
    long pendingLiveVerifications,
    long pendingRegistrations,
    Double averagePlatformRating
) {}

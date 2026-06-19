package com.pentastack.skillsync.availability.dto;

import java.time.LocalDateTime;

public record AvailabilitySlotResponse(long id, LocalDateTime startTime, LocalDateTime endTime, boolean isBooked) {}

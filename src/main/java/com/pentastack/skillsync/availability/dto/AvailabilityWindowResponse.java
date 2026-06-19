package com.pentastack.skillsync.availability.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityWindowResponse(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {}

package com.pentastack.skillsync.availability.dto;

import java.time.LocalDate;
import java.util.List;

public record DayAvailabilityResponse(LocalDate date, List<AvailabilitySlotResponse> slots) {}

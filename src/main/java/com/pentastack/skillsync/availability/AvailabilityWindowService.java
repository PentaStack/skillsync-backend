package com.pentastack.skillsync.availability;

import com.pentastack.skillsync.availability.dto.AvailabilitySlotResponse;
import com.pentastack.skillsync.availability.dto.AvailabilityWindowRequest;
import com.pentastack.skillsync.availability.dto.AvailabilityWindowResponse;
import com.pentastack.skillsync.availability.dto.DayAvailabilityResponse;
import com.pentastack.skillsync.domain.MentorAvailability;
import com.pentastack.skillsync.domain.MentorProfile;
import com.pentastack.skillsync.domain.ReviewSession;
import com.pentastack.skillsync.domain.SessionStatus;
import com.pentastack.skillsync.domain.repository.MentorAvailabilityRepository;
import com.pentastack.skillsync.domain.repository.MentorProfileRepository;
import com.pentastack.skillsync.domain.repository.ReviewSessionRepository;
import com.pentastack.skillsync.exception.ApiException;
import com.pentastack.skillsync.model.Role;
import com.pentastack.skillsync.model.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvailabilityWindowService {

    private final MentorAvailabilityRepository windowRepo;
    private final MentorProfileRepository mentorRepo;
    private final UserRepository userRepo;
    private final ReviewSessionRepository sessionRepo;

    public AvailabilityWindowService(
        MentorAvailabilityRepository windowRepo,
        MentorProfileRepository mentorRepo,
        @Qualifier("modelUserRepository") UserRepository userRepo,
        ReviewSessionRepository sessionRepo
    ) {
        this.windowRepo = windowRepo;
        this.mentorRepo = mentorRepo;
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
    }

    @Transactional(readOnly = true)
    public Long getMyMentorId(String email) {
        return mentorRepo.findByUser_Email(email)
            .map(MentorProfile::getId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mentor profile not found"));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityWindowResponse> listWindows(Long mentorId) {
        if (!mentorRepo.existsById(mentorId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Mentor not found");
        }
        return windowRepo.findByMentor_Id(mentorId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AvailabilityWindowResponse createWindow(String email, Long mentorId, AvailabilityWindowRequest req) {
        MentorProfile mentor = mentorRepo.findById(mentorId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mentor not found"));
        authorizeWrite(email, mentorId);
        validate(mentorId, req.dayOfWeek(), req.startTime(), req.endTime(), null);
        return toResponse(windowRepo.save(new MentorAvailability(mentor, req.dayOfWeek(), req.startTime(), req.endTime())));
    }

    @Transactional
    public AvailabilityWindowResponse updateWindow(String email, Long mentorId, Long windowId, AvailabilityWindowRequest req) {
        if (!mentorRepo.existsById(mentorId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Mentor not found");
        }
        authorizeWrite(email, mentorId);
        MentorAvailability window = windowRepo.findById(windowId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Window not found"));
        if (!window.getMentor().getId().equals(mentorId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Window not found for this mentor");
        }
        validate(mentorId, req.dayOfWeek(), req.startTime(), req.endTime(), windowId);
        window.update(req.dayOfWeek(), req.startTime(), req.endTime());
        return toResponse(windowRepo.save(window));
    }

    @Transactional
    public void deleteWindow(String email, Long mentorId, Long windowId) {
        if (!mentorRepo.existsById(mentorId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Mentor not found");
        }
        authorizeWrite(email, mentorId);
        MentorAvailability window = windowRepo.findById(windowId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Window not found"));
        if (!window.getMentor().getId().equals(mentorId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Window not found for this mentor");
        }
        windowRepo.delete(window);
    }

    @Transactional(readOnly = true)
    public DayAvailabilityResponse computeSlots(Long mentorId, LocalDate date) {
        if (!mentorRepo.existsById(mentorId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Mentor not found");
        }
        List<MentorAvailability> windows = windowRepo.findByMentor_IdAndDayOfWeek(mentorId, date.getDayOfWeek());
        if (windows.isEmpty()) {
            return new DayAvailabilityResponse(date, List.of());
        }
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        List<ReviewSession> booked = sessionRepo.findByMentor_IdAndStatusAndStartTimeBetween(
            mentorId, SessionStatus.SCHEDULED, dayStart, dayEnd);

        List<AvailabilitySlotResponse> slots = new ArrayList<>();
        for (MentorAvailability w : windows) {
            LocalDateTime blockStart = date.atTime(w.getStartTime());
            LocalDateTime windowEnd = date.atTime(w.getEndTime());
            while (!blockStart.plusMinutes(45).isAfter(windowEnd)) {
                final LocalDateTime slotStart = blockStart;
                final LocalDateTime slotEnd = blockStart.plusMinutes(45);
                boolean blocked = booked.stream().anyMatch(s ->
                    slotStart.isBefore(s.getEndTime()) && slotEnd.isAfter(s.getStartTime()));
                if (!blocked) {
                    // ponytail: synthetic id — throwaway React key, not persisted
                    slots.add(new AvailabilitySlotResponse(slotStart.toEpochSecond(ZoneOffset.UTC), slotStart, slotEnd, false));
                }
                blockStart = slotEnd;
            }
        }
        return new DayAvailabilityResponse(date, slots);
    }

    private void authorizeWrite(String email, Long mentorId) {
        boolean isAdmin = userRepo.findByEmail(email)
            .map(u -> u.getRole() == Role.ADMIN)
            .orElse(false);
        if (isAdmin) return;
        MentorProfile profile = mentorRepo.findByUser_Email(email)
            .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Access denied"));
        if (!profile.getId().equals(mentorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private void validate(Long mentorId, DayOfWeek day, LocalTime start, LocalTime end, Long excludeId) {
        if (!end.isAfter(start)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }
        if (Duration.between(start, end).toMinutes() < 45) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Window must be at least 45 minutes");
        }
        boolean overlaps = windowRepo.findByMentor_IdAndDayOfWeek(mentorId, day).stream()
            .filter(w -> excludeId == null || !w.getId().equals(excludeId))
            .anyMatch(w -> start.isBefore(w.getEndTime()) && end.isAfter(w.getStartTime()));
        if (overlaps) {
            throw new ApiException(HttpStatus.CONFLICT, "Window overlaps with an existing window on that day");
        }
    }

    private AvailabilityWindowResponse toResponse(MentorAvailability w) {
        return new AvailabilityWindowResponse(w.getId(), w.getDayOfWeek(), w.getStartTime(), w.getEndTime());
    }
}

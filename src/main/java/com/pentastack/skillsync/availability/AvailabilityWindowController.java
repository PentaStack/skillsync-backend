package com.pentastack.skillsync.availability;

import com.pentastack.skillsync.availability.dto.AvailabilityWindowRequest;
import com.pentastack.skillsync.availability.dto.AvailabilityWindowResponse;
import com.pentastack.skillsync.availability.dto.DayAvailabilityResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentors")
public class AvailabilityWindowController {

    private final AvailabilityWindowService service;

    public AvailabilityWindowController(AvailabilityWindowService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public Map<String, Long> getMyId(Principal principal) {
        return Map.of("mentorId", service.getMyMentorId(principal.getName()));
    }

    @GetMapping("/{mentorId}/availability")
    public DayAvailabilityResponse getSlots(
        @PathVariable Long mentorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.computeSlots(mentorId, date);
    }

    @GetMapping("/{mentorId}/availability/windows")
    public List<AvailabilityWindowResponse> list(@PathVariable Long mentorId) {
        return service.listWindows(mentorId);
    }

    @PostMapping("/{mentorId}/availability/windows")
    public ResponseEntity<AvailabilityWindowResponse> create(
        @PathVariable Long mentorId,
        @Valid @RequestBody AvailabilityWindowRequest request,
        Principal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.createWindow(principal.getName(), mentorId, request));
    }

    @PutMapping("/{mentorId}/availability/windows/{windowId}")
    public AvailabilityWindowResponse update(
        @PathVariable Long mentorId,
        @PathVariable Long windowId,
        @Valid @RequestBody AvailabilityWindowRequest request,
        Principal principal
    ) {
        return service.updateWindow(principal.getName(), mentorId, windowId, request);
    }

    @DeleteMapping("/{mentorId}/availability/windows/{windowId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long mentorId,
        @PathVariable Long windowId,
        Principal principal
    ) {
        service.deleteWindow(principal.getName(), mentorId, windowId);
        return ResponseEntity.noContent().build();
    }
}

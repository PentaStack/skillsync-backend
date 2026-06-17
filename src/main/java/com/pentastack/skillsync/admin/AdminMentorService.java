package com.pentastack.skillsync.admin;

import com.pentastack.skillsync.admin.dto.*;
import com.pentastack.skillsync.common.dto.PagedResponse;
import com.pentastack.skillsync.domain.ReviewSession;
import com.pentastack.skillsync.domain.StudentProfile;
import com.pentastack.skillsync.domain.repository.ReviewSessionRepository;
import com.pentastack.skillsync.domain.repository.StackRepository;
import com.pentastack.skillsync.exception.ApiException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMentorService {

    private final com.pentastack.skillsync.model.repository.MentorProfileRepository modelMentorProfileRepository;
    private final com.pentastack.skillsync.domain.repository.MentorProfileRepository domainMentorProfileRepository;
    private final com.pentastack.skillsync.domain.repository.StudentProfileRepository domainStudentProfileRepository;
    private final StackRepository stackRepository;
    private final ReviewSessionRepository reviewSessionRepository;

    public AdminMentorService(
        com.pentastack.skillsync.model.repository.MentorProfileRepository modelMentorProfileRepository,
        com.pentastack.skillsync.domain.repository.MentorProfileRepository domainMentorProfileRepository,
        com.pentastack.skillsync.domain.repository.StudentProfileRepository domainStudentProfileRepository,
        StackRepository stackRepository,
        ReviewSessionRepository reviewSessionRepository
    ) {
        this.modelMentorProfileRepository = modelMentorProfileRepository;
        this.domainMentorProfileRepository = domainMentorProfileRepository;
        this.domainStudentProfileRepository = domainStudentProfileRepository;
        this.stackRepository = stackRepository;
        this.reviewSessionRepository = reviewSessionRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminRegistrationMentorResponse> getPendingRegistrations(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<com.pentastack.skillsync.model.MentorProfile> result =
            modelMentorProfileRepository.findByIsVerified(false, pageable);

        List<AdminRegistrationMentorResponse> items = result.getContent().stream()
            .map(mp -> new AdminRegistrationMentorResponse(
                mp.getId(),
                mp.getName(),
                mp.getUser().getEmail(),
                resolveStackName(mp.getStackId()),
                mp.getUser().getCreatedAt(),
                mp.isVerified()
            ))
            .toList();

        return new PagedResponse<>(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Transactional
    public void updateRegistrationVerification(Long id, boolean isVerified) {
        com.pentastack.skillsync.model.MentorProfile mentor = modelMentorProfileRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Registration mentor not found"));
        mentor.setVerified(isVerified);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminLiveMentorResponse> getPendingLiveVerifications(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<com.pentastack.skillsync.domain.MentorProfile> result =
            domainMentorProfileRepository.findByAvailable(false, pageable);

        List<AdminLiveMentorResponse> items = result.getContent().stream()
            .map(mp -> new AdminLiveMentorResponse(
                mp.getId(),
                mp.getDisplayName(),
                mp.getUser().getEmail(),
                mp.getStack() != null ? mp.getStack().getName() : "Unknown stack",
                mp.isAvailable(),
                mp.getRating()
            ))
            .toList();

        return new PagedResponse<>(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Transactional
    public void updateLiveVerification(Long id, boolean isVerified) {
        com.pentastack.skillsync.domain.MentorProfile mentor = domainMentorProfileRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Live mentor not found"));
        mentor.updateProfile(mentor.getTitle(), mentor.getBio(), mentor.getHourlyRate(), isVerified);
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        long totalSessions = reviewSessionRepository.count();
        long activeMentors = domainMentorProfileRepository.countByAvailable(true);
        long pendingLiveVerifications = domainMentorProfileRepository.countByAvailable(false);
        long pendingRegistrations = modelMentorProfileRepository.countByIsVerified(false);
        Double avgRating = domainMentorProfileRepository.findAverageRatingByAvailable(true).orElse(null);

        return new AdminStatsResponse(
            totalSessions,
            activeMentors,
            pendingLiveVerifications,
            pendingRegistrations,
            avgRating
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminMentorListResponse> getAllMentors(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<com.pentastack.skillsync.domain.MentorProfile> result =
            domainMentorProfileRepository.findAllWithDetails(pageable);

        Map<Long, Long> sessionCounts = reviewSessionRepository.countSessionsGroupedByMentorId()
            .stream()
            .collect(Collectors.toMap(arr -> (Long) arr[0], arr -> (Long) arr[1]));

        List<AdminMentorListResponse> items = result.getContent().stream()
            .map(mp -> new AdminMentorListResponse(
                mp.getId(),
                mp.getDisplayName(),
                mp.getUser().getEmail(),
                mp.getStack() != null ? mp.getStack().getName() : "Unknown stack",
                mp.getTitle(),
                mp.getBio(),
                mp.isAvailable(),
                mp.getRating(),
                mp.getHourlyRate(),
                sessionCounts.getOrDefault(mp.getId(), 0L)
            ))
            .toList();

        return new PagedResponse<>(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AdminMentorDetailResponse getMentorDetail(Long id) {
        com.pentastack.skillsync.domain.MentorProfile mp = domainMentorProfileRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mentor not found"));

        List<ReviewSession> sessions = reviewSessionRepository.findByMentor_IdOrderByStartTimeDesc(id);

        return new AdminMentorDetailResponse(
            mp.getId(),
            mp.getDisplayName(),
            mp.getUser().getEmail(),
            mp.getStack() != null ? mp.getStack().getName() : "Unknown stack",
            mp.getStack() != null ? mp.getStack().getId() : null,
            mp.getTitle(),
            mp.getBio(),
            mp.isAvailable(),
            mp.getRating(),
            mp.getHourlyRate(),
            sessions.size(),
            sessions.stream().map(s -> new AdminSessionSummaryResponse(
                s.getId(),
                s.getMentor().getDisplayName(),
                s.getStudent().getDisplayName(),
                s.getStudent().getUser().getEmail(),
                s.getStartTime().toString(),
                s.getEndTime().toString(),
                s.getStatus().name(),
                s.getDescription()
            )).toList()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminStudentListResponse> getAllStudents(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<StudentProfile> result = domainStudentProfileRepository.findAllWithUser(pageable);

        Map<Long, Long> sessionCounts = reviewSessionRepository.countSessionsGroupedByStudentUserId()
            .stream()
            .collect(Collectors.toMap(arr -> (Long) arr[0], arr -> (Long) arr[1]));

        List<AdminStudentListResponse> items = result.getContent().stream()
            .map(sp -> new AdminStudentListResponse(
                sp.getId(),
                sp.getDisplayName(),
                sp.getUser().getEmail(),
                sessionCounts.getOrDefault(sp.getUser().getId(), 0L)
            ))
            .toList();

        return new PagedResponse<>(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AdminStudentDetailResponse getStudentDetail(Long id) {
        StudentProfile sp = domainStudentProfileRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));

        List<ReviewSession> sessions = reviewSessionRepository.findByStudent_User_IdOrderByStartTimeDesc(sp.getUser().getId());

        return new AdminStudentDetailResponse(
            sp.getId(),
            sp.getDisplayName(),
            sp.getUser().getEmail(),
            sessions.size(),
            sessions.stream().map(s -> new AdminSessionSummaryResponse(
                s.getId(),
                s.getMentor().getDisplayName(),
                s.getStudent().getDisplayName(),
                s.getStudent().getUser().getEmail(),
                s.getStartTime().toString(),
                s.getEndTime().toString(),
                s.getStatus().name(),
                s.getDescription()
            )).toList()
        );
    }

    private String resolveStackName(Long stackId) {
        if (stackId == null) return "Unknown stack";
        return stackRepository.findById(stackId)
            .map(com.pentastack.skillsync.domain.Stack::getName)
            .orElse("Unknown stack");
    }
}

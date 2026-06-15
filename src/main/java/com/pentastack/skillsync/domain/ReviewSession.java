package com.pentastack.skillsync.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "review_sessions")
public class ReviewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(length = 2000)
    private String evaluationNotes;

    @OneToOne(mappedBy = "session", fetch = FetchType.LAZY)
    private SessionAuditLog auditLog;

    protected ReviewSession() {}

    public ReviewSession(MentorProfile mentor, StudentProfile student, LocalDateTime startTime, String description) {
        this.mentor = mentor;
        this.student = student;
        this.startTime = startTime;
        this.endTime = startTime.plusMinutes(45);
        this.description = description;
        this.status = SessionStatus.SCHEDULED;
    }

    public void cancel() { this.status = SessionStatus.CANCELED; }
    public void complete(String notes) {
        this.status = SessionStatus.COMPLETED;
        this.evaluationNotes = notes;
    }
    public void updateDescription(String description) {
        this.description = description;
    }
    public void reschedule(LocalDateTime startTime) {
        this.startTime = startTime;
        this.endTime = startTime.plusMinutes(45);
    }
}

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "session_audit_logs")
public class SessionAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ReviewSession session;

    @Column
    private String predictedTag;

    private Double confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditStatus status;

    @Column(length = 2000)
    private String errorMessage;

    @Column
    private Long latencyMs;

    protected SessionAuditLog() {}

    public SessionAuditLog(ReviewSession session, String predictedTag, Double confidenceScore, AuditStatus status, String errorMessage, Long latencyMs) {
        this.session = session;
        this.predictedTag = predictedTag;
        this.confidenceScore = confidenceScore;
        this.status = status;
        this.errorMessage = errorMessage;
        this.latencyMs = latencyMs;
    }

    public Long getId() { return id; }
    public ReviewSession getSession() { return session; }
    public String getPredictedTag() { return predictedTag; }
    public Double getConfidenceScore() { return confidenceScore; }
    public AuditStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Long getLatencyMs() { return latencyMs; }
}

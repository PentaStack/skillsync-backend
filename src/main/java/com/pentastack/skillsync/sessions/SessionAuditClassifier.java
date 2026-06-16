package com.pentastack.skillsync.sessions;

public interface SessionAuditClassifier {
    AuditClassificationResult classify(String submissionDescription);
}

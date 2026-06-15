package com.pentastack.skillsync.sessions;

import org.springframework.stereotype.Component;

@Component
public class SessionAuditClassifierImpl implements SessionAuditClassifier {
    @Override
    public AuditClassificationResult classify(String submissionDescription) {
        long start = System.currentTimeMillis();
        String lower = submissionDescription.toLowerCase();
        String tag = lower.contains("race") ? "ASYNC_RACE" : lower.contains("review") ? "CODE_REVIEW" : "GENERAL_REVIEW";
        double confidence = lower.contains("race") ? 0.91 : lower.contains("review") ? 0.86 : 0.74;
        return AuditClassificationResult.success(tag, confidence, System.currentTimeMillis() - start);
    }
}

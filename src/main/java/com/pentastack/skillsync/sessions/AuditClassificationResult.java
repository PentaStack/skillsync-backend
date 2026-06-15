package com.pentastack.skillsync.sessions;

public record AuditClassificationResult(
    boolean successful,
    String predictedTag,
    Double confidenceScore,
    Long latencyMs,
    String errorMessage
) {
    public static AuditClassificationResult success(String predictedTag, Double confidenceScore, long latencyMs) {
        return new AuditClassificationResult(true, predictedTag, confidenceScore, latencyMs, null);
    }

    public static AuditClassificationResult failed(String errorMessage, long latencyMs) {
        return new AuditClassificationResult(false, null, null, latencyMs, errorMessage);
    }
}

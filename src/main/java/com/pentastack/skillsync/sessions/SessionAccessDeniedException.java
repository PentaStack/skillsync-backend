package com.pentastack.skillsync.sessions;

public class SessionAccessDeniedException extends RuntimeException {
    public SessionAccessDeniedException(String message) {
        super(message);
    }
}

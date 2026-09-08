package com.jobfinder.application;

public enum ApplicationStatus {
    /** Prepared, not yet reviewed by the user. */
    DRAFT,
    /** Reviewed and ready for the user to send. */
    READY,
    SENT,
    /** Sent a while ago with no reply - worth a nudge. */
    FOLLOW_UP,
    INTERVIEW,
    REJECTED,
    CLOSED
}

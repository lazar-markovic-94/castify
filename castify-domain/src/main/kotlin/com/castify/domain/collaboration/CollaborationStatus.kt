package com.castify.domain.collaboration

/**
 * Status of a collaboration/project
 */
enum class CollaborationStatus {
    /**
     * Initial state - client has sent a proposal to talent
     */
    PROPOSED,

    /**
     * Talent has accepted the proposal
     */
    ACCEPTED,

    /**
     * Talent has declined the proposal
     */
    DECLINED,

    /**
     * Work has started on the project
     */
    IN_PROGRESS,

    /**
     * Project has been successfully completed
     */
    COMPLETED,

    /**
     * Project has been canceled by either party
     */
    CANCELED
}

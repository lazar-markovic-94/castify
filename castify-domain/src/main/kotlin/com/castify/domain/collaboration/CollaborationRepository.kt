package com.castify.domain.collaboration

import java.util.*

/**
 * Repository for managing collaborations
 */
interface CollaborationRepository {
    fun save(collaboration: Collaboration): Collaboration
    fun findById(collaborationId: UUID): Collaboration?
    fun findByClientUserId(clientUserId: UUID): List<Collaboration>
    fun findByTalentProfileId(talentProfileId: UUID): List<Collaboration>
    fun findByClientUserIdAndStatus(clientUserId: UUID, status: CollaborationStatus): List<Collaboration>
    fun findByTalentProfileIdAndStatus(talentProfileId: UUID, status: CollaborationStatus): List<Collaboration>
    fun countByTalentProfileIdAndStatus(talentProfileId: UUID, status: CollaborationStatus): Int
}

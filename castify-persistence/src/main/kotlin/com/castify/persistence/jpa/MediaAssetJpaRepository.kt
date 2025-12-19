package com.castify.persistence.jpa

import com.castify.persistence.entity.MediaAssetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MediaAssetJpaRepository : JpaRepository<MediaAssetEntity, UUID> {
    fun findByUploadedByUserId(uploadedByUserId: UUID): List<MediaAssetEntity>
}

package org.akm.filedownloader.repository

import org.akm.filedownloader.model.ThreadInfo
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadRepository : JpaRepository<ThreadInfo, UUID> {
    fun findAllByStatus(status: String): List<ThreadInfo>
}

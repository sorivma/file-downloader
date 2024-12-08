package org.akm.filedownloader.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity
data class ThreadInfo(
    @Id
    val id: UUID = UUID.randomUUID(),
    val ftpServer: String,
    val ftpUsername: String,
    val ftpPassword: String,
    val remoteDirectory: String,
    val s3DirectoryPath: String,
    val cronExpression: String,
    var status: String = "Created",
    var lastRun: Date? = null
)

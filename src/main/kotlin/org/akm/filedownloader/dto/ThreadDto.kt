package org.akm.filedownloader.dto

data class ThreadDto(
    val ftpServer: String,
    val ftpUsername: String,
    val ftpPassword: String,
    val remoteDirectory: String,
    val s3DirectoryPath: String,
    val cronExpression: String
)


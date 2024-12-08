package org.akm.filedownloader.service

import io.minio.MinioClient
import io.minio.PutObjectArgs
import jakarta.transaction.Transactional
import org.akm.filedownloader.dto.ThreadDto
import org.akm.filedownloader.model.ThreadInfo
import org.akm.filedownloader.repository.ThreadRepository
import org.apache.commons.net.ftp.FTPClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val minioClient: MinioClient,
    @Value("\${aws.s3.bucket-name}") private val s3Bucket: String
) {
    private val executorService: ExecutorService = Executors.newFixedThreadPool(10)

    private val activeThreads: MutableMap<UUID, Future<*>> = mutableMapOf()

    @Transactional
    fun createThread(threadDto: ThreadDto) {
        val threadInfo = ThreadInfo(
            ftpServer = threadDto.ftpServer,
            ftpUsername = threadDto.ftpUsername,
            ftpPassword = threadDto.ftpPassword,
            cronExpression = threadDto.cronExpression,
            status = "Scheduled",
            lastRun = null,
            remoteDirectory = threadDto.remoteDirectory,
            s3DirectoryPath = threadDto.s3DirectoryPath,
        )
        threadRepository.save(threadInfo)
    }

    // Start thread manually (cron job or one-off execution)
    fun startThread(threadId: UUID) {
        val threadInfo = threadRepository.findById(threadId).orElseThrow {
            IllegalArgumentException("Thread not found")
        }

        if (threadInfo.status == "Running") {
            throw IllegalStateException("Thread is already running")
        }

        threadInfo.status = "Running"
        threadRepository.save(threadInfo)

        val future = executorService.submit {
            try {
                runThread(threadInfo)
            } catch (e: Exception) {
                threadInfo.status = "Failed: ${e.message}"
                threadRepository.save(threadInfo)
            }
        }
        activeThreads[threadId] = future
    }

    // Stop thread manually
    fun stopThread(threadId: UUID) {
        val threadInfo = threadRepository.findById(threadId).orElseThrow {
            IllegalArgumentException("Thread not found")
        }

        val future = activeThreads[threadId]
        future?.cancel(true)

        threadInfo.status = "Stopped"
        threadRepository.save(threadInfo)

        activeThreads.remove(threadId)
    }

    @Transactional
    fun deleteThread(threadId: UUID) {
        val threadInfo = threadRepository.findById(threadId).orElseThrow {
            IllegalArgumentException("Thread not found")
        }

        stopThread(threadId)

        threadRepository.delete(threadInfo)
    }

    fun getThreadStatus(threadId: UUID): ThreadInfo {
        return threadRepository.findById(threadId).orElseThrow {
            IllegalArgumentException("Thread not found")
        }
    }

    @Scheduled(cron = "\${custom.cron.expression}")
    fun runScheduledThreads() {
        val threadsToRun = threadRepository.findAllByStatus("Scheduled")
        threadsToRun.forEach { threadInfo ->
            // If the thread is scheduled, run it
            executorService.submit {
                try {
                    runThread(threadInfo)
                } catch (e: Exception) {
                    threadInfo.status = "Failed: ${e.message}"
                    threadRepository.save(threadInfo)
                }
            }
        }
    }


    private fun runThread(threadInfo: ThreadInfo) {
        try {

            val ftpClient = FTPClient()
            ftpClient.connect(threadInfo.ftpServer)
            ftpClient.login(threadInfo.ftpUsername, threadInfo.ftpPassword)


            val file = downloadFileFromFTP(ftpClient)


            val s3ObjectKey = "uploaded-file-${System.currentTimeMillis()}.txt"
            uploadFileToS3(file, s3ObjectKey)

            threadInfo.status = "Completed"
            threadInfo.lastRun = Date()
            threadRepository.save(threadInfo)

            ftpClient.disconnect()
        } catch (e: Exception) {
            threadInfo.status = "Failed: ${e.message}"
            threadRepository.save(threadInfo)
        }
    }


    private fun downloadFileFromFTP(ftpClient: FTPClient): File {
        val remoteFilePath = "/path/to/remote/file.txt"

        val localFile = File.createTempFile("ftp_download", ".tmp")
        ftpClient.retrieveFile(remoteFilePath, localFile.outputStream())

        return localFile
    }

    private fun uploadFileToS3(file: File, s3ObjectKey: String) {
        val putObjectArgs = PutObjectArgs.builder()
            .bucket(s3Bucket)
            .`object`(s3ObjectKey)
            .stream(FileInputStream(file), file.length(), -1)
            .build()

        minioClient.putObject(putObjectArgs)
    }
}

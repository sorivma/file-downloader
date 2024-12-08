package org.akm.filedownloader.controller

import org.akm.filedownloader.service.ThreadService
import org.akm.filedownloader.dto.ThreadDto
import org.akm.filedownloader.model.ThreadInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {

    // Endpoint to create a new thread
    @PostMapping
    fun createThread(@RequestBody threadDto: ThreadDto): ResponseEntity<ThreadInfo> {
        try {
            threadService.createThread(threadDto)
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(null)
        }
    }


    @DeleteMapping("/{id}")
    fun deleteThread(@PathVariable id: UUID): ResponseEntity<String> {
        return try {
            threadService.deleteThread(id)
            ResponseEntity.ok("Thread deleted successfully")
        } catch (e: Exception) {
            ResponseEntity.status(404).body("Thread not found")
        }
    }

    @GetMapping("/{id}")
    fun getThreadStatus(@PathVariable id: UUID): ResponseEntity<ThreadInfo> {
        return try {
            val threadInfo = threadService.getThreadStatus(id)
            ResponseEntity.ok(threadInfo)
        } catch (e: Exception) {
            ResponseEntity.status(404).body(null)
        }
    }

    @PostMapping("/{id}/start")
    fun startThread(@PathVariable id: UUID): ResponseEntity<String> {
        return try {
            threadService.startThread(id)
            ResponseEntity.ok("Thread started successfully")
        } catch (e: Exception) {
            ResponseEntity.status(404).body("Thread not found")
        }
    }

    @PostMapping("/{id}/stop")
    fun stopThread(@PathVariable id: UUID): ResponseEntity<String> {
        return try {
            threadService.stopThread(id)
            ResponseEntity.ok("Thread stopped successfully")
        } catch (e: Exception) {
            ResponseEntity.status(404).body("Thread not found")
        }
    }
}
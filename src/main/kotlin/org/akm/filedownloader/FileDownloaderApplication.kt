package org.akm.filedownloader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FileDownloaderApplication

fun main(args: Array<String>) {
    runApplication<FileDownloaderApplication>(*args)
}

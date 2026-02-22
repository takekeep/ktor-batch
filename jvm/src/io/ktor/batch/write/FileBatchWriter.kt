package io.ktor.batch.write

import io.ktor.utils.io.charsets.Charset
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class FileBatchWriter(
    private val filePath: String,
    private val charset: Charset = Charsets.UTF_8,
    private val createIfNotExists: Boolean = true
) {
    private val file = File(filePath)

    init {
        if (createIfNotExists && !file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }
    }

    fun write(content: String) {
        try {
            Files.write(
                file.toPath(),
                content.toByteArray(charset),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            )
        } catch (e: Exception) {
            throw WriterException("Failed to write to file: $filePath", e)
        }
    }

    fun writeLines(lines: List<String>) {
        write(lines.joinToString(System.lineSeparator()) + System.lineSeparator())
    }

    fun clear() {
        try {
            file.writeText("", charset)
        } catch (e: Exception) {
            throw WriterException("Failed to clear file: $filePath", e)
        }
    }

    fun worker(): suspend () -> Unit = {
        // Có thể tùy chỉnh logic ghi file tại đây
    }

    class WriterException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

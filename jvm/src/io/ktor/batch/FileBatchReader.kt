package io.ktor.batch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

/**
 * Copilot support
 * Simple coroutine-friendly file reader for batch jobs.
 *
 * Usage:
 * val reader = FileBatchReader(Path.of("/path/to/file")) { line -> process(line) }
 * val worker = reader.worker()
 */
class FileBatchReader(
    private val path: Path,
    private val onLine: suspend (String) -> Unit = {}
) {

    /**
     * Read the file line-by-line on the IO dispatcher and call [onLine] for each line.
     * If the file does not exist this method returns immediately.
     */
    suspend fun readAll() {
        // Perform blocking IO on the IO dispatcher
        withContext(Dispatchers.IO) {
            if (!Files.exists(path)) return@withContext

            Files.newBufferedReader(path).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    onLine(line)
                    line = reader.readLine()
                }
            }
        }
    }

    /**
     * Return a suspend worker that can be passed to SimpleBatch as the repeating task.
     */
    fun worker(): suspend () -> Unit = { readAll() }
}


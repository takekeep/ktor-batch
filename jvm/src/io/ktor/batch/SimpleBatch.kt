package io.ktor.batch

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.log
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class SimpleBatch private constructor(
    private val intervalMs: Long,
    private val worker: suspend () -> Unit
) {

    private var scope: CoroutineScope? = null
    private var job: Job? = null

    private fun start(app: Application) {
        job = SupervisorJob()
        scope = CoroutineScope(app.coroutineContext + job!!)

        scope?.launch {
            while (isActive) {
                try {
                    worker()
                } catch (e: Exception) {
                    app.log.error("Error in SimpleBatch worker", e)
                }
                delay(intervalMs)
            }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        scope = null
    }

    companion object Plugin : BaseApplicationPlugin<Application, Configuration, SimpleBatch> {

        override val key = AttributeKey<SimpleBatch>("SimpleBatch")

        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit
        ): SimpleBatch {
            val config = Configuration().apply(configure)

            val plugin = SimpleBatch(
                intervalMs = config.intervalMs,
                worker = config.worker
            )

            // Start when application is ready
            pipeline.environment.monitor.subscribe(ApplicationStarted) {
                plugin.start(pipeline)
            }

            // Cancel when application stops
            pipeline.environment.monitor.subscribe(ApplicationStopped) {
                plugin.stop()
            }

            return plugin
        }
    }

    class Configuration {
        var intervalMs: Long = 10_000
        var worker: suspend () -> Unit = {}

        // Optional: provide a FileBatchReader to supply the worker implementation
        var fileReader: FileBatchReader? = null
            set(value) {
                field = value
                if (value != null) {
                    worker = value.worker()
                }
            }
    }
}

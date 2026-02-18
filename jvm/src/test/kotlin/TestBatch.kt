package test.kotlin

import io.ktor.batch.SimpleBatch
import io.ktor.server.testing.testApplication
import io.ktor.server.application.*
import kotlin.test.Test
import kotlin.test.assertNotNull

class TestBatch {

    @Test
    fun testBatchInstall() = testApplication {
        application {
            install(SimpleBatch) {
                intervalMs = 100
                worker = {}
            }
        }

        val plugin = application {
            pluginOrNull(SimpleBatch)
        }
        assertNotNull(plugin)
    }
}

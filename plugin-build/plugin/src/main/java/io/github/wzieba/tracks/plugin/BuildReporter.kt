package io.github.wzieba.tracks.plugin

import io.github.wzieba.tracks.plugin.analytics.AnalyticsReporter
import io.github.wzieba.tracks.plugin.analytics.Emojis.FAILURE_ICON
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logger
import java.util.concurrent.TimeUnit

class BuildReporter(
    private val logger: Logger,
    private val analyticsReporter: AnalyticsReporter
) {

    @Suppress("TooGenericExceptionCaught")
    fun report(buildData: BuildData, username: String?, customEventName: String?) {
        try {
            reportMeasured(buildData, username, customEventName)
        } catch (ex: Exception) {
            logger.warn("$FAILURE_ICON Build time reporting failed: $ex")
        }
    }

    private fun reportMeasured(buildData: BuildData, username: String?, customEventName: String?) {
        val start = nowMillis()

        reportInternal(buildData, username, customEventName)

        val reportingOverhead = nowMillis() - start
        logger.info("Reporting overhead: $reportingOverhead ms.")
    }

    private fun reportInternal(
        buildData: BuildData,
        username: String?,
        customEventName: String?,
    ) {
        runBlocking {
            analyticsReporter.report(logger, buildData, username, customEventName)
        }
    }

    private fun nowMillis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}

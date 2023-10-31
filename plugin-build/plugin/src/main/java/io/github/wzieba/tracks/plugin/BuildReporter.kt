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
    fun report(buildData: BuildData, username: String) {
        try {
            reportMeasured(buildData, username)
        } catch (ex: Exception) {
            logger.warn("$FAILURE_ICON Build time reporting failed: $ex")
        }
    }

    private fun reportMeasured(buildData: BuildData, username: String) {
        val start = nowMillis()

        reportInternal(buildData, username)

        val reportingOverhead = nowMillis() - start
        logger.info("Reporting overhead: $reportingOverhead ms.")
    }

    private fun reportInternal(buildData: BuildData, username: String) {
        runBlocking {
            analyticsReporter.report(logger, buildData, username)
        }
    }

    private fun nowMillis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}

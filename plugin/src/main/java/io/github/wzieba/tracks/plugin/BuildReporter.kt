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
    fun report(buildData: BuildData, username: String, gradleScanUrl: String?) {
        try {
            reportMeasured(buildData, username, gradleScanUrl)
        } catch (ex: Exception) {
            logger.warn("\n$FAILURE_ICON Build time reporting failed: $ex")
        }
    }

    private fun reportMeasured(buildData: BuildData, username: String, gradleScanUrl: String?) {
        val start = nowMillis()

        reportInternal(buildData, username, gradleScanUrl)

        val reportingOverhead = nowMillis() - start
        logger.info("Reporting overhead: $reportingOverhead ms.")
    }

    private fun reportInternal(buildData: BuildData, username: String, gradleScanId: String?) {
        runBlocking {
            analyticsReporter.report(logger, buildData, username, gradleScanId)
        }
    }

    private fun nowMillis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}

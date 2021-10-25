package io.github.wzieba.tracks.plugin

import io.github.wzieba.tracks.plugin.analytics.AnalyticsReporter
import io.github.wzieba.tracks.plugin.analytics.Emojis.FAILURE_ICON
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class BuildReporter(
    private val analyticsReporter: AnalyticsReporter
) {

    @Suppress("TooGenericExceptionCaught")
    fun report(buildData: BuildData, username: String, customEventName: String?, debug: Boolean) {
        try {
            reportMeasured(buildData, username, customEventName, debug)
        } catch (ex: Exception) {
            println("$FAILURE_ICON Build time reporting failed: $ex")
        }
    }

    private fun reportMeasured(buildData: BuildData, username: String, customEventName: String?, debug: Boolean) {
        val start = nowMillis()

        reportInternal(buildData, username, customEventName, debug)

        val reportingOverhead = nowMillis() - start
        if (debug) {
            println(
                "Reporting overhead: $reportingOverhead ms."
            )
        }
    }

    private fun reportInternal(
        buildData: BuildData,
        username: String,
        customEventName: String?,
        debug: Boolean
    ) {
        runBlocking {
            analyticsReporter.report(buildData, username, customEventName, debug)
        }
    }

    private fun nowMillis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}

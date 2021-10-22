package com.ncorti.kotlin.gradle.template.plugin

import com.ncorti.kotlin.gradle.template.plugin.analytics.AnalyticsReporter
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class BuildReporter(
    private val analyticsReporter: AnalyticsReporter
) {
    fun report(buildData: BuildData) {
        try {
            reportMeasured(buildData)
        } catch (ex: Exception) {
            println("Build time reporting failed: $ex")
        }
    }

    private fun reportMeasured(buildData: BuildData) {
        val start = nowMillis()

        reportInternal(buildData)

        val reportingOverhead = nowMillis() - start
        println("$STOPWATCH_ICON Build time '${buildData.buildTime} ms' reported to $analyticsReporter in $reportingOverhead ms.$STOPWATCH_ICON")
    }

    private fun reportInternal(buildData: BuildData) {
        runBlocking {
            analyticsReporter.report(buildData)
        }
    }

    private fun nowMillis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

    companion object {
        private const val STOPWATCH_ICON = "\u23F1"
    }
}

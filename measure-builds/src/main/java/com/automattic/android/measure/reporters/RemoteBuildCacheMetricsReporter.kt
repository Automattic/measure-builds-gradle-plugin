package com.automattic.android.measure.reporters

import com.automattic.android.measure.logging.Emojis
import org.gradle.api.logging.Logging
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object RemoteBuildCacheMetricsReporter {

    private val logger = Logging.getLogger(RemoteBuildCacheMetricsReporter::class.java)
    private val LOG_SAVINGS_THRESHOLD = 500.milliseconds

    @JvmStatic
    fun report(metricsReporter: MetricsReport) {

        metricsReporter.report.remoteBuildCacheData?.let { data ->
            val totalSavings = data.totalSavings
            val avoidances = data.avoidances

            if (totalSavings > LOG_SAVINGS_THRESHOLD) {
                logger.lifecycle("\n${Emojis.ROCKET_ICON} Sum of estimated remote cache savings: $totalSavings (not actual saved build duration). Top savings:")
                logger.lifecycle(String.format(Locale.US, "%-15s %s", "Saved", "Task"))
                avoidances.take(3).forEach { (task, avoidance) ->
                    logger.lifecycle(String.format("%-15s %s", avoidance, task))
                }
            } else if (totalSavings < Duration.ZERO) {
                logger.lifecycle("\n${Emojis.FAILURE_ICON} It's estimated that remote cache added ${totalSavings.absoluteValue} to the build. Assert you have stable internet connection.")
            }

        }

    }
}

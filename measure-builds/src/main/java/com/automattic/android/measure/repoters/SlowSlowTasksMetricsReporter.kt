package com.automattic.android.measure.repoters

import com.automattic.android.measure.InMemoryReport
import com.automattic.android.measure.logging.Emojis
import com.automattic.android.measure.models.MeasuredTask
import org.gradle.api.logging.Logging
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

object SlowSlowTasksMetricsReporter : MetricsReporter {
    private val logger = Logging.getLogger(SlowSlowTasksMetricsReporter::class.java)
    override suspend fun report(
        report: InMemoryReport,
        gradleScanId: String?,
        parameters: MetricsDispatcher.Parameters
    ) {
        if (report.executionData.buildTime == 0L) {
            return
        }
        val slowTasks =
            report.executionData.executedTasks.sortedByDescending { it.duration }
                .chunked(atMostLoggedTasks).firstOrNull()
                ?: return

        logger.lifecycle("\n${Emojis.TURTLE_ICON} ${slowTasks.size} slowest tasks were: ")

        logger.lifecycle(
            String.format(
                Locale.US,
                "%-15s %-15s %s",
                "Duration",
                "% of build",
                "Task"
            )
        )
        slowTasks.forEach {
            @Suppress("MagicNumber")
            logger.lifecycle(
                "%-15s %-15s %s".format(
                    Locale.US,
                    readableDuration(it),
                    "${(it.duration.inWholeMilliseconds * 100 / report.executionData.buildTime).toInt()}%",
                    it.name,
                )
            )
        }
    }

    private fun readableDuration(it: MeasuredTask) =
        if (it.duration < 1.seconds) {
            "${it.duration.inWholeMilliseconds}ms"
        } else {
            "${it.duration.inWholeSeconds}s"
        }

    private const val atMostLoggedTasks = 5
}
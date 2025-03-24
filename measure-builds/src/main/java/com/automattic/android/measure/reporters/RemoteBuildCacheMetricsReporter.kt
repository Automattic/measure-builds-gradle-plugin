package com.automattic.android.measure.reporters

import org.gradle.api.logging.Logging
import kotlin.time.Duration.Companion.milliseconds

object RemoteBuildCacheMetricsReporter {

    private val logger = Logging.getLogger(RemoteBuildCacheMetricsReporter::class.java)

    @JvmStatic
    fun report(metricsReporter: MetricsReport) {
        val (originExecutionTimes, remoteLoadTimes) = metricsReporter.report.remoteBuildCacheData

        val avoidanceMap = originExecutionTimes.mapNotNull {
            val remoteLoadTime = remoteLoadTimes[it.key]
            if (remoteLoadTime != null) {
                val timeAvoidance = it.value.executionTime - remoteLoadTime
                it.value.name to timeAvoidance.milliseconds
            } else {
                null
            }
        }

        logger.lifecycle(avoidanceMap.toString())

    }
}
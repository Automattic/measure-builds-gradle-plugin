package com.automattic.android.measure.models

import com.automattic.android.measure.tools.IntervalMeasurer
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Be extra careful when adding new parameters to build data. It's resolved during configuration
 * phase, so it's necessary to make sure that changes in those parameters will invalidate
 * the configuration cache. You can do this in BuildTimePluginConfigurationCacheTests.
 *
 * Details: https://discuss.gradle.org/t/is-there-a-way-to-get-up-to-date-gradle-start-parameters-with-configuration-cache/47806/
 */
@Serializable
data class BuildData(
    val user: String,
    val gradleVersion: String,
    val operatingSystem: String,
    val environment: Environment,
    val isConfigurationCache: Boolean,
    val includedBuildsNames: List<String>,
    val architecture: String,
)

@Serializable
data class ExecutionData(
    val buildTime: Long,
    val failed: Boolean,
    val failure: String?,
    val executedTasks: List<MeasuredTask>,
    val requestedTasks: List<String>,
    val buildFinishedTimestamp: Long,
    val configurationPhaseDuration: Long,
)

@Serializable
data class RemoteBuildCacheData(
    val originExecutions: MutableMap<String, OriginExecutionTaskData> = hashMapOf(),
    val remoteLoads: MutableMap<String, DownloadEvent> = hashMapOf(),
    val unpackTimes: MutableMap<String, Long> = hashMapOf()
) {
    val remoteLoadTimes: Map<String, Long>
        get() = remoteLoads.mapValues { it.value.endTime - it.value.startTime }

    /**
     * Maps cache keys to avoidance values, representing the estimated time saved by using the remote build cache.
     * The avoidance value is calculated by subtracting the remote cache retrieval time
     * from the original execution time.
     */
    val avoidances: List<Pair<String, Duration>>
        get() = originExecutions.mapNotNull {
            val remoteLoadTime = remoteLoadTimes[it.key]
            val unpackTime = unpackTimes[it.key]
            if (remoteLoadTime != null && unpackTime != null) {
                val timeAvoidance = it.value.executionTime - remoteLoadTime - unpackTime
                it.value.name to timeAvoidance.milliseconds
            } else {
                null
            }
        }.sortedByDescending { it.second }

    /**
     * The total estimated time saved across all tasks by using the remote build cache.
     * Calculated as the sum of all individual avoidance values in milliseconds.
     *
     * Note: This calculation assumes sequential execution and does not account for parallelization.
     */
    val totalSavings
        get() = avoidances.sumOf { it.second.inWholeMilliseconds }.milliseconds

    private val clockTimeDownloadDuration: Duration
        get() = remoteLoads.values.toList().map { it.startTime to it.endTime }
            .let { IntervalMeasurer.findTotalTime(it) }.milliseconds

    private val totalArchiveSize: Long
        get() = remoteLoads.values.sumOf { it.archiveSize }

    /**
     * Estimates the download speed while considering task parallelization.
     * This calculation does not account for any pauses between task executions.
     *
     * The estimation is based on the time between the start of the first fetch
     * and the completion of the last fetch. The total fetched artifact size is
     * divided by this duration to approximate the download speed.
     *
     * Note: This is an estimated value and may not reflect the exact download speed,
     * but it still provides a useful approximation. Inspired by
     * https://github.com/runningcode/gradle-doctor/blob/2e61538beeda9d8859e20861e18b227508edfd6f/doctor-plugin/src/main/java/com/osacky/doctor/BuildCacheConnectionMeasurer.kt#L15
     */
    val estimatedDownloadSpeed: Double?
        get() {
            return if (clockTimeDownloadDuration.inWholeMilliseconds > 0) {
                totalArchiveSize.toDouble() / clockTimeDownloadDuration.inWholeSeconds
            } else {
                null
            }
        }
}

@Serializable
data class OriginExecutionTaskData(
    val name: String,
    val executionTime: Long
)

@Serializable
data class DownloadEvent(
    val startTime: Long,
    val endTime: Long,
    val archiveSize: Long
)

enum class Environment {
    IDE,
    CI,
    CMD
}

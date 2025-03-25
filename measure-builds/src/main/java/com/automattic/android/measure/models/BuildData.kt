package com.automattic.android.measure.models

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
    val remoteLoadTimes: MutableMap<String, Long> = hashMapOf()
) {

    /**
     * Maps cache keys to avoidance values, representing the estimated time saved by using the remote build cache.
     * The avoidance value is calculated by subtracting the time required to retrieve the task output from the remote cache
     * from the original execution time.
     */
    val avoidances: List<Pair<String, Duration>>
        get() = originExecutions.mapNotNull {
            val remoteLoadTime = remoteLoadTimes[it.key]
            if (remoteLoadTime != null) {
                val timeAvoidance = it.value.executionTime - remoteLoadTime
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
}

@Serializable
data class OriginExecutionTaskData(
    val name: String,
    val executionTime: Long
)

enum class Environment {
    IDE,
    CI,
    CMD
}

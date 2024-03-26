package com.automattic.android.measure.models

import kotlinx.serialization.Serializable

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

enum class Environment {
    IDE,
    CI,
    CMD
}

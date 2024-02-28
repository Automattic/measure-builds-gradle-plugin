package com.automattic.android.measure.models

import com.automattic.android.measure.MeasureBuildsExtension
import kotlinx.serialization.Serializable

@Serializable
data class BuildData(
    val forProject: MeasureBuildsExtension.AutomatticProject,
    val user: String,
    val tasks: List<String>,
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
    val tasks: List<MeasuredTask>,
    val buildFinishedTimestamp: Long,
    val configurationPhaseDuration: Long,
)

enum class Environment {
    IDE,
    CI,
    CMD
}

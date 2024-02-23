package com.automattic.android.measure.models

import com.automattic.android.measure.MeasureBuildsExtension
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.gradle.api.provider.Provider

@Serializable
data class BuildData(
    val user: String,
    val tasks: List<String>,
    val daemonsRunning: Int,
    val thisDaemonBuilds: Int,
    val gradleVersion: String,
    val operatingSystem: String,
    val environment: Environment,
    val isConfigureOnDemand: Boolean,
    val isConfigurationCache: Boolean,
    val isBuildCache: Boolean,
    val maxWorkers: Int,
    val includedBuildsNames: List<String>,
    val architecture: String,
){
    @Transient
    lateinit var projectProvider: Provider<MeasureBuildsExtension.AutomatticProject>

    val forProject: MeasureBuildsExtension.AutomatticProject
        get() = projectProvider.get()
}

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

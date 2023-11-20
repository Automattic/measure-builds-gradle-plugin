package io.github.wzieba.tracks.plugin

data class BuildData(
    val forProject: TracksExtension.AutomatticProject,
    val buildTime: Long,
    val tasks: List<String>,
    val failed: Boolean,
    val failure: Throwable?,
    val daemonsRunning: Int,
    val thisDaemonBuilds: Int,
    val gradleVersion: String,
    val operatingSystem: String,
    val environment: Environment,
    val isConfigureOnDemand: Boolean,
    val isConfigurationCache: Boolean,
    val isBuildCache: Boolean,
    val maxWorkers: Int,
    val taskStatistics: TaskStatistics,
    val buildDataCollectionOverhead: Long,
    val includedBuildsNames: List<String>,
    val architecture: String,
    val buildFinishedTimestamp: Long,
)

enum class Environment {
    IDE,
    CI,
    CMD
}

data class TaskStatistics(
    val upToDate: Int,
    val fromCache: Int,
    val executed: Int
)

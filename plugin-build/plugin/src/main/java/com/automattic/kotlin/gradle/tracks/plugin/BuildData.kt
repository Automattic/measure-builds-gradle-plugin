package com.automattic.kotlin.gradle.tracks.plugin

data class BuildData(
    val action: String,
    val buildTime: Long,
    val tasks: List<String>,
    val failed: Boolean,
    val failure: Throwable?,
    val daemonsRunning: Int,
    val thisDaemonBuilds: Int,
    val hostname: String,
    val gradleVersion: String,
    val operatingSystem: String,
    val environment: Environment,
    val isConfigureOnDemand: Boolean,
    val isConfigurationCache: Boolean,
    val isBuildCache: Boolean,
    val maxWorkers: Int,
    val taskStatistics: TaskStatistics,
    val buildDataCollectionOverhead: Long
)

enum class Environment {
    IDE,
    CI,
    CMD
}

data class TaskStatistics(
    val total: Int,
    val upToDate: Int,
    val fromCache: Int,
    val executed: Int
)

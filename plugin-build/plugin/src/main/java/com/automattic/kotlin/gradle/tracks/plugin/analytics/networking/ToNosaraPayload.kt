package com.automattic.kotlin.gradle.tracks.plugin.analytics.networking

import com.automattic.kotlin.gradle.tracks.plugin.BuildData
import com.automattic.kotlin.gradle.tracks.plugin.TracksExtension

fun BuildData.toTracksPayload(username: String) = TracksPayload(
    events = listOf(
        Event(
            eventName = this.forProject.toEventName(),
            eventTimestamp = System.currentTimeMillis(),
            tasks = this.tasks,
            gradleAction = this.action,
            buildTime = this.buildTime,
            failed = this.failed,
            failure = this.failure?.message,
            daemonsRunning = this.daemonsRunning,
            thisDaemonBuilds = this.thisDaemonBuilds,
            gradleVersion = this.gradleVersion,
            operatingSystem = this.operatingSystem,
            environment = this.environment,
            userType = username,
            tasksTotal = this.taskStatistics.total,
            tasksUpToDate = this.taskStatistics.upToDate,
            tasksFromCache = this.taskStatistics.fromCache,
            tasksExecuted = this.taskStatistics.executed,
            isConfigureOnDemand = this.isConfigureOnDemand,
            isConfigurationCache = this.isConfigurationCache,
            isBuildCache = this.isBuildCache,
            maxWorkers = this.maxWorkers
        )
    )
)

private fun TracksExtension.AutomatticProject.toEventName() = when (this) {
    TracksExtension.AutomatticProject.WooCommerce -> "wc_android_build_finished"
    TracksExtension.AutomatticProject.WordPress -> "wp_android_build_finished"
    TracksExtension.AutomatticProject.Simplenote -> "sn_android_build_finished"
    TracksExtension.AutomatticProject.DayOne -> "do_android_build_finished"
    TracksExtension.AutomatticProject.PocketCasts -> "pc_android_build_finished"
    TracksExtension.AutomatticProject.TracksGradle -> "tg_android_build_finished"
}

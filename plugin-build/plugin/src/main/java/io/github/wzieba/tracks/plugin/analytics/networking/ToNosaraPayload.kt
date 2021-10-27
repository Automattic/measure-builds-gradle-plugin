package io.github.wzieba.tracks.plugin.analytics.networking

import io.github.wzieba.tracks.plugin.BuildData
import io.github.wzieba.tracks.plugin.TracksExtension

fun BuildData.toTracksPayload(customEventName: String?, username: String) = TracksPayload(
    events = listOf(
        Event(
            eventName = customEventName ?: this.forProject.toEventName(),
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
            maxWorkers = this.maxWorkers,
            includedBuilds = this.includedBuildsNames,
            userId = -1 // Tracks require a non-null user id
        )
    )
)

private fun TracksExtension.AutomatticProject.toEventName() = when (this) {
    TracksExtension.AutomatticProject.WooCommerce -> "woocommerceandroid_gradle_build_finished"
    TracksExtension.AutomatticProject.WordPress -> "wpandroid_gradle_build_finished"
    TracksExtension.AutomatticProject.DayOne -> "dayone_gradle_build_finished"
}

package com.automattic.kotlin.gradle.tracks.plugin.analytics.nosara

import com.automattic.kotlin.gradle.tracks.plugin.BuildData

fun BuildData.toNosaraPayload() = NosaraPayload(
    events = listOf(
        Event(
            eventName = "wc_android_build_finished",
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
            userType = "anon",
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

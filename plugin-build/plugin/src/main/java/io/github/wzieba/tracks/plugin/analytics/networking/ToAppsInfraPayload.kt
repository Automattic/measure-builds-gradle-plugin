package io.github.wzieba.tracks.plugin.analytics.networking

import io.github.wzieba.tracks.plugin.BuildData

@Suppress("LongMethod")
fun BuildData.toAppsInfraPayload(user: String) = GroupedAppsMetrics(
    meta = listOf(
        AppsMetric(
            name = "User",
            value = user
        ),
        AppsMetric(
            name = "Project",
            value = this.forProject.name
        )
    ),
    metrics = listOf(
        AppsMetric(
            name = "Requested tasks",
            value = this.tasks.joinToString(separator = ",")
        ),
        AppsMetric(
            name = "Performed Gradle action",
            value = this.action
        ),
        AppsMetric(
            name = "Build time (ms)",
            value = this.buildTime.toString()
        ),
        AppsMetric(
            name = "Build status",
            value = if (this.failed) "Failure" else "Success"
        ),
        AppsMetric(
            name = "Failure message",
            value = this.failure.toString()
        ),
        AppsMetric(
            name = "Number of running daemons",
            value = this.daemonsRunning.toString()
        ),
        AppsMetric(
            name = "Daemon's build count",
            value = this.thisDaemonBuilds.toString()
        ),
        AppsMetric(
            name = "Gradle version",
            value = this.gradleVersion
        ),
        AppsMetric(
            name = "Operating system",
            value = this.operatingSystem
        ),
        AppsMetric(
            name = "Environment",
            value = this.environment.toString()
        ),
        AppsMetric(
            name = "Total number of tasks",
            value = this.taskStatistics.total.toString()
        ),
        AppsMetric(
            name = "Up to date tasks",
            value = this.taskStatistics.upToDate.toString()
        ),
        AppsMetric(
            name = "Tasks from cache",
            value = this.taskStatistics.fromCache.toString()
        ),
        AppsMetric(
            name = "Executed tasks",
            value = this.taskStatistics.executed.toString()
        ),
        AppsMetric(
            name = "Is configure on demand",
            value = this.isConfigureOnDemand.toString()
        ),
        AppsMetric(
            name = "Is configuration cache",
            value = this.isConfigurationCache.toString()
        ),
        AppsMetric(
            name = "Is build cache",
            value = this.isBuildCache.toString()
        ),
        AppsMetric(
            name = "Max workers",
            value = this.maxWorkers.toString()
        ),
        AppsMetric(
            name = "Build data collection overhead",
            value = this.buildDataCollectionOverhead.toString()
        ),
        AppsMetric(
            name = "Included builds",
            value = this.includedBuildsNames.joinToString(separator = ",").ifEmpty { "none" }
        ),
        AppsMetric(
            name = "Architecture",
            value = this.architecture
        ),
    )
)

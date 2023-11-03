package io.github.wzieba.tracks.plugin.analytics.networking

import io.github.wzieba.tracks.plugin.BuildData

@Suppress("LongMethod")
fun BuildData.toAppsInfraPayload(user: String): GroupedAppsMetrics {
    val projectKey = this.forProject.name.lowercase()

    val meta = mapOf(
        "user" to user, "project" to projectKey
    )

    val metrics = mapOf(
        "requested-tasks" to tasks.joinToString(separator = ","),
        "performed-gradle-action" to action,
        "build-time-ms" to buildTime.toString(),
        "build-status" to if (failed) "Failure" else "Success",
        "failure-message" to failure.toString(),
        "number-of-running-daemons" to daemonsRunning.toString(),
        "daemons-build-count" to thisDaemonBuilds.toString(),
        "gradle-version" to gradleVersion,
        "operating-system" to operatingSystem,
        "environment" to environment.name,
        "total-tasks-number" to taskStatistics.total.toString(),
        "up-to-date-tasks" to taskStatistics.upToDate.toString(),
        "cached-tasks" to taskStatistics.fromCache.toString(),
        "executed-tasks" to taskStatistics.executed.toString(),
        "configure-on-demand" to isConfigureOnDemand.toString(),
        "configuration-cache" to isConfigurationCache.toString(),
        "build-cache" to isBuildCache.toString(),
        "max-workers" to maxWorkers.toString(),
        "build-data-collection-overhead-ms" to buildDataCollectionOverhead.toString(),
        "included-builds" to includedBuildsNames.joinToString(separator = ",").ifEmpty { "none" },
        "architecture" to architecture
    )

    return GroupedAppsMetrics(
        meta = meta.map { (key, value) -> AppsMetric(name = "$projectKey-$key", value = value) },
        metrics = metrics.map { (key, value) ->
            AppsMetric(
                name = "$projectKey-$key", value = value
            )
        },
    )
}

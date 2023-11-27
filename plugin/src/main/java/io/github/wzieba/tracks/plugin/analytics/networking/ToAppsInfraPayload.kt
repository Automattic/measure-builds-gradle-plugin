package io.github.wzieba.tracks.plugin.analytics.networking

import io.github.wzieba.tracks.plugin.Report

fun Report.toAppsInfraPayload(gradleScanId: String?): GroupedAppsMetrics {
    val projectKey = buildData.forProject.name.lowercase()

    val meta = mapOf(
        "user" to buildData.user,
        "project" to projectKey,
        "environment" to buildData.environment.name,
        "architecture" to buildData.architecture,
        "operating-system" to buildData.operatingSystem,
    )

    val metrics = mapOf(
        "requested-tasks" to buildData.tasks.joinToString(separator = ","),
        "build-time-ms" to executionData.buildTime.toString(),
        "build-status" to if (executionData.failed) "Failure" else "Success",
        "failure-message" to executionData.failure.toString(),
        "number-of-running-daemons" to buildData.daemonsRunning.toString(),
        "daemons-build-count" to buildData.thisDaemonBuilds.toString(),
        "gradle-version" to buildData.gradleVersion,
        "up-to-date-tasks" to executionData.taskStatistics.upToDate.toString(),
        "cached-tasks" to executionData.taskStatistics.fromCache.toString(),
        "executed-tasks" to executionData.taskStatistics.executed.toString(),
        "configure-on-demand" to buildData.isConfigureOnDemand.toString(),
        "configuration-cache" to buildData.isConfigurationCache.toString(),
        "build-cache" to buildData.isBuildCache.toString(),
        "max-workers" to buildData.maxWorkers.toString(),
        "build-data-collection-overhead-ms" to buildData.buildDataCollectionOverhead.toString(),
        "included-builds" to buildData.includedBuildsNames.joinToString(separator = ",")
            .ifEmpty { "none" },
        "build-finished-at" to executionData.buildFinishedTimestamp.toString(),
        "gradle-scan-id" to gradleScanId.orEmpty(),
    )

    return GroupedAppsMetrics(
        meta = meta.map { (key, value) ->
            AppsMetric(
                name = "$projectKey-$key",
                // Apps Metrics doesn't allow metric value to be empty
                value = value.ifBlank { "null" }
            )
        },
        metrics = metrics.map { (key, value) ->
            AppsMetric(
                name = "$projectKey-$key",
                // Apps Metrics doesn't allow metric value to be empty
                value = value.ifBlank { "null" }
            )
        },
    )
}

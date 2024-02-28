package com.automattic.android.measure.networking

import com.automattic.android.measure.InMemoryReport
import com.automattic.android.measure.models.MeasuredTask.State.EXECUTED
import com.automattic.android.measure.models.MeasuredTask.State.IS_FROM_CACHE
import com.automattic.android.measure.models.MeasuredTask.State.UP_TO_DATE

fun InMemoryReport.toAppsInfraPayload(gradleScanId: String?): GroupedAppsMetrics {
    val projectKey = buildData.forProject.name.lowercase()

    val meta = mapOf(
        "user" to buildData.user,
        "project" to projectKey,
        "environment" to buildData.environment.name,
        "architecture" to buildData.architecture,
        "operating-system" to buildData.operatingSystem,
    )

    val taskGroups = executionData.tasks.groupBy { it.state }

    val metrics = mapOf(
        "requested-tasks" to buildData.tasks.joinToString(separator = ","),
        "build-time-ms" to executionData.buildTime.toString(),
        "build-status" to if (executionData.failed) "Failure" else "Success",
        "failure-message" to executionData.failure.toString(),
        "gradle-version" to buildData.gradleVersion,
        "up-to-date-tasks" to taskGroups[UP_TO_DATE].sizeOrZero(),
        "cached-tasks" to taskGroups[IS_FROM_CACHE].sizeOrZero(),
        "executed-tasks" to taskGroups[EXECUTED].sizeOrZero(),
        "configuration-cache" to buildData.isConfigurationCache.toString(),
        "build-cache" to buildData.isBuildCache.toString(),
        "included-builds" to buildData.includedBuildsNames.joinToString(separator = ",")
            .ifEmpty { "none" },
        "build-finished-at" to executionData.buildFinishedTimestamp.toString(),
        "gradle-scan-id" to gradleScanId.orEmpty(),
        "configuration-duration" to executionData.configurationPhaseDuration.toString()
    )

    val tasks = executionData.tasks.map {
        "task-${it.name}" to it.duration.inWholeMilliseconds.toString()
    }

    return GroupedAppsMetrics(
        meta = meta.map { (key, value) ->
            AppsMetric(
                name = "$projectKey-$key",
                // Apps Metrics doesn't allow metric value to be empty
                value = value.ifBlank { "null" }
            )
        },
        metrics = (metrics + tasks).map { (key, value) ->
            AppsMetric(
                name = "$projectKey-$key",
                // Apps Metrics doesn't allow metric value to be empty
                value = value.ifBlank { "null" }
            )
        },
    )
}

private fun <K> Collection<K>?.sizeOrZero() = "${this?.size ?: 0}"

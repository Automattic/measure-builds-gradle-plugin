@file:Suppress("UnstableApiUsage")

package io.github.wzieba.tracks.plugin.analytics

import io.github.wzieba.tracks.plugin.BuildTaskService
import io.github.wzieba.tracks.plugin.ExecutionData
import io.github.wzieba.tracks.plugin.InMemoryReport
import io.github.wzieba.tracks.plugin.TaskStatistics
import kotlinx.coroutines.runBlocking
import org.gradle.api.flow.BuildWorkResult
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import kotlin.jvm.optionals.getOrNull

class BuildFinishedFlowAction : FlowAction<BuildFinishedFlowAction.Parameters> {
    interface Parameters : FlowParameters {
        @get:Input
        val startTime: Property<Long>

        @get:Input
        val buildWorkResult: Property<Provider<BuildWorkResult>>

        @get:Input
        val analyticsReporter: Property<AnalyticsReporter>

        @get:Input
        val authToken: Property<String>

        @get:Input
        val attachGradleScanId: Property<Boolean>

        @get:ServiceReference
        val buildTaskService: Property<BuildTaskService>
    }

    override fun execute(parameters: Parameters) {
        val finish = System.currentTimeMillis()

        val result = parameters.buildWorkResult.get().get()
        val statistics = parameters.buildTaskService.get().taskStatistics

        val buildTime = finish - parameters.startTime.get()

        val executionData = ExecutionData(
            buildTime = buildTime,
            failed = result.failure.isPresent,
            failure = result.failure.getOrNull(),
            taskStatistics = TaskStatistics(
                statistics.upToDateTaskCount,
                statistics.fromCacheTaskCount,
                statistics.executedTasksCount
            ),
            buildFinishedTimestamp = finish
        )

        InMemoryReport.executionDataStore = executionData

        if (parameters.attachGradleScanId.get() == false) {
            runBlocking {
                parameters.analyticsReporter.get().report(
                    report = InMemoryReport,
                    authToken = parameters.authToken.get(),
                    gradleScanId = null
                )
            }
        }
    }
}

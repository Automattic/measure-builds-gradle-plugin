@file:Suppress("UnstableApiUsage")

package io.github.wzieba.tracks.plugin.analytics

import io.github.wzieba.tracks.plugin.BuildDataFactory.buildData
import io.github.wzieba.tracks.plugin.TracksExtension
import org.gradle.api.flow.BuildWorkResult
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.internal.tasks.execution.statistics.TaskExecutionStatistics
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.invocation.DefaultGradle

class BuildFinishedFlowAction :
    FlowAction<BuildFinishedFlowAction.Parameters> {
    interface Parameters : FlowParameters {
        @get:Input
        val ext: Property<TracksExtension>

        @get:Input
        val gradle: Property<DefaultGradle>

        @get:Input
        val buildWorkResult: Property<Provider<BuildWorkResult>>

        @get:Input
        val taskStatistics: Property<TaskExecutionStatistics>
    }

    override fun execute(parameters: Parameters) {
        val extension = parameters.ext.get()
        val gradle = parameters.gradle.get()

        val buildData = buildData(
            parameters.buildWorkResult.get().get(),
            gradle,
            parameters.taskStatistics.get(),
            extension.automatticProject.get(),
            gradle.includedBuilds.toList().map { it.name }
        )

        gradle.extensions.add("automattic_build_data", buildData)

        if (extension.sendMetricsOnBuildFinished.get()) {
            extension.reportBuild(null)
        }
    }
}

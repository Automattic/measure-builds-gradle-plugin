@file:Suppress("UnstableApiUsage")

package io.github.wzieba.tracks.plugin.analytics

import io.github.wzieba.tracks.plugin.BuildDataFactory.buildData
import io.github.wzieba.tracks.plugin.BuildTaskService
import io.github.wzieba.tracks.plugin.TracksExtension
import org.gradle.api.flow.BuildWorkResult
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
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
        val buildTaskService: Property<BuildTaskService>
    }

    override fun execute(parameters: Parameters) {
        val extension = parameters.ext.get()
        val gradle = parameters.gradle.get()

        val buildData = buildData(
            parameters.buildWorkResult.get().get(),
            gradle,
            parameters.buildTaskService.get().taskStatistics,
            extension.automatticProject.get(),
            gradle.includedBuilds.toList().map { it.name }
        )

        gradle.extensions.add("automattic_build_data", buildData)

        if (extension.sendMetricsOnBuildFinished.get()) {
            extension.reportBuild(null)
        }
    }
}

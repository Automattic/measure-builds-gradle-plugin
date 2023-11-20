@file:Suppress("UnstableApiUsage")

package io.github.wzieba.tracks.plugin.analytics

import io.github.wzieba.tracks.plugin.BuildDataFactory.buildData
import io.github.wzieba.tracks.plugin.BuildReporter
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
        val gradle: Property<DefaultGradle>

        @get:Input
        val buildWorkResult: Property<Provider<BuildWorkResult>>

        @get:Input
        val buildTaskService: Property<BuildTaskService>

        @get:Input
        val buildReporter: Property<BuildReporter>

        @get:Input
        val username: Property<String>

        @get:Input
        val enabled: Property<Boolean?>

        @get:Input
        val automatticProject: Property<TracksExtension.AutomatticProject>
    }

    override fun execute(parameters: Parameters) {
        if (parameters.enabled.orNull != true) return
        val buildData = buildData(
            parameters.buildWorkResult.get().get(),
            parameters.gradle.get(),
            parameters.buildTaskService.get().taskStatistics,
            parameters.automatticProject.get(),
            parameters.gradle.get().includedBuilds.toList().map { it.name }
        )
        parameters.buildReporter.get().report(buildData, parameters.username.get())
    }
}

package io.github.wzieba.tracks.plugin

import io.github.wzieba.tracks.plugin.analytics.BuildFinishedFlowAction
import io.github.wzieba.tracks.plugin.analytics.networking.AppsMetricsReporter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.flow.FlowActionSpec
import org.gradle.api.flow.FlowProviders
import org.gradle.api.flow.FlowScope
import org.gradle.api.provider.Provider
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.invocation.DefaultGradle
import javax.inject.Inject
import kotlin.time.ExperimentalTime

const val EXTENSION_NAME = "tracks"

@Suppress("UnstableApiUsage")
@ExperimentalTime
class BuildTimePlugin @Inject constructor(
    private val registry: BuildEventsListenerRegistry,
    private val flowScope: FlowScope,
    private val flowProviders: FlowProviders,
) : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions.create(
                EXTENSION_NAME,
                TracksExtension::class.java,
                project,
                BuildReporter(project.logger, AppsMetricsReporter(project))
            )

        val serviceProvider: Provider<BuildTaskService> =
            project.gradle.sharedServices.registerIfAbsent(
                "taskEvents",
                BuildTaskService::class.java
            ) { }
        registry.onTaskCompletion(serviceProvider)

        flowScope.always(
            BuildFinishedFlowAction::class.java
        ) { spec: FlowActionSpec<BuildFinishedFlowAction.Parameters> ->

            spec.parameters.apply {
                buildWorkResult.set(flowProviders.buildWorkResult)
                gradle.set(project.gradle as DefaultGradle)
                taskStatistics.set(serviceProvider.get().taskStatistics)
                ext.set(extension)
            }
        }
    }
}

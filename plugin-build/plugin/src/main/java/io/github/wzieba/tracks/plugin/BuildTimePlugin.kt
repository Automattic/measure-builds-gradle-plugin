package io.github.wzieba.tracks.plugin

import io.github.wzieba.tracks.plugin.analytics.networking.AppsMetricsReporter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.build.event.BuildEventsListenerRegistry
import javax.inject.Inject
import kotlin.time.ExperimentalTime

const val EXTENSION_NAME = "tracks"

@ExperimentalTime
class BuildTimePlugin @Inject constructor(private val registry: BuildEventsListenerRegistry) : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, TracksExtension::class.java, project)

        val buildTimeListener = BuildTimeListener(
            buildDataFactory = BuildDataFactory,
            buildReporter = BuildReporter(project.logger, AppsMetricsReporter(project)),
            tracksExtension = extension,
            includedBuilds = project.gradle.includedBuilds,
            logger = project.logger
        )
        project.gradle.addBuildListener(buildTimeListener)

        val serviceProvider: Provider<BuildTaskService> =
            project.gradle.sharedServices.registerIfAbsent(
                "taskEvents", BuildTaskService::class.java
            ) { spec -> }
        registry.onTaskCompletion(serviceProvider)
    }
}

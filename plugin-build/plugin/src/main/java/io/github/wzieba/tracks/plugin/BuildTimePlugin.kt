package io.github.wzieba.tracks.plugin

import io.github.wzieba.tracks.plugin.analytics.networking.TracksReporter
import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.time.ExperimentalTime

const val EXTENSION_NAME = "tracks"

@ExperimentalTime
abstract class BuildTimePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, TracksExtension::class.java, project)

        val buildTimeListener = BuildTimeListener(
            buildDataFactory = BuildDataFactory,
            buildReporter = BuildReporter(TracksReporter()),
            tracksExtension = extension,
            includedBuilds = project.gradle.includedBuilds
        )
        project.gradle.addBuildListener(buildTimeListener)
    }
}

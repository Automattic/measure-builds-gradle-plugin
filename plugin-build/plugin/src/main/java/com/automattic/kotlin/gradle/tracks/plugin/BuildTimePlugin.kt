package com.automattic.kotlin.gradle.tracks.plugin

import com.automattic.kotlin.gradle.tracks.plugin.analytics.networking.TracksReporter
import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.time.ExperimentalTime

const val EXTENSION_NAME = "tracks"

@ExperimentalTime
abstract class BuildTimePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, TracksExtension::class.java, project)

        val buildTimeListener = BuildTimeListener(BuildDataFactory, BuildReporter(TracksReporter()), extension)
        project.gradle.addBuildListener(buildTimeListener)
    }
}

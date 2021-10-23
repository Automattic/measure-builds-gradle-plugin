package com.automattic.kotlin.gradle.tracks.plugin

import com.automattic.kotlin.gradle.tracks.plugin.analytics.nosara.NosaraReporter
import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "tracks"


abstract class BuildTimePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the 'template' extension object
        val extension = project.extensions.create(EXTENSION_NAME, TemplateExtension::class.java, project)

        val buildTimeListener = BuildTimeListener(BuildDataFactory, reporter())
        project.gradle.addBuildListener(buildTimeListener)
    }

    private fun reporter(): BuildReporter {
        return BuildReporter(NosaraReporter())
//        return BuildReporter(AnalyticsReporter.create("Build Time Plugin"))
    }
}

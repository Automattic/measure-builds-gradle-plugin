package com.ncorti.kotlin.gradle.template.plugin

import com.ncorti.kotlin.gradle.template.plugin.analytics.nosara.NosaraReporter
import org.gradle.api.Plugin
import org.gradle.api.Project


abstract class BuildTimePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val buildTimeListener = BuildTimeListener(BuildDataFactory, reporter())
        project.gradle.addBuildListener(buildTimeListener)
    }

    private fun reporter(): BuildReporter {
        return BuildReporter(NosaraReporter())
//        return BuildReporter(AnalyticsReporter.create("Build Time Plugin"))
    }
}

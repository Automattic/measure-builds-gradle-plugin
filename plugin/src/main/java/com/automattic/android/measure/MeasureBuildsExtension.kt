package com.automattic.android.measure

import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class MeasureBuildsExtension(project: Project) {

    private val objects = project.objects

    val automatticProject: Property<AutomatticProject> =
        objects.property(AutomatticProject::class.java)

    val enabled: Property<Boolean> = objects.property(Boolean::class.java)

    val obfuscateUsername: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * If `true`, then the metrics will be sent at build finish,
     * orchestrated by Gradle Enterprise plugin, attaching
     * Gradle Build Scan id to metrics.
     *
     * If `false`, then metrics will be sent at build finish by
     * @see io.github.wzieba.tracks.plugin.analytics.BuildFinishedFlowAction
     */
    val attachGradleScanId: Property<Boolean> = objects.property(Boolean::class.java)

    enum class AutomatticProject {
        WooCommerce, WordPress, DayOne, PocketCasts, Tumblr, FluxC
    }
}

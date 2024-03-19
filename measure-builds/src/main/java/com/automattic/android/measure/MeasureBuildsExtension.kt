package com.automattic.android.measure

import com.automattic.android.measure.reporters.MetricsReport
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class MeasureBuildsExtension(project: Project) {

    private val objects = project.objects

    val enable: Property<Boolean> = objects.property(Boolean::class.java)

    val obfuscateUsername: Property<Boolean> = objects.property(Boolean::class.java)

    @Suppress("UNCHECKED_CAST")
    val reporterProperty: Property<Action<MetricsReport>> = objects.property(Action::class.java) as Property<Action<MetricsReport>>

    fun buildMetricsReported(action: Action<MetricsReport>) {
        reporterProperty.set(action)
    }

    /**
     * If `true`, then the metrics will be sent at build finish,
     * orchestrated by Gradle Enterprise plugin, attaching
     * Gradle Build Scan id to metrics.
     *
     * If `false`, then metrics will be sent at build finish by
     * @see io.github.wzieba.tracks.plugin.analytics.BuildFinishedFlowAction
     */
    val attachGradleScanId: Property<Boolean> = objects.property(Boolean::class.java)
}

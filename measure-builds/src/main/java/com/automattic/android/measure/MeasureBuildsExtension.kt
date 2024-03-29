package com.automattic.android.measure

import com.automattic.android.measure.reporters.MetricsReport
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class MeasureBuildsExtension(project: Project) {

    private val objects = project.objects

    /**
     * Enable or disable the plugin.
     */
    val enable: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Enable or disable obfuscation of the username. If `true`, the username will be obfuscated by SHA-1.
     */
    val obfuscateUsername: Property<Boolean> = objects.property(Boolean::class.java)

    @Suppress("UNCHECKED_CAST")
    internal val buildMetricsPreparedAction: Property<Action<MetricsReport>> =
        objects.property(Action::class.java) as Property<Action<MetricsReport>>

    /**
     * Action to be executed when the build metrics are ready.
     */
    fun buildMetricsPrepared(action: Action<MetricsReport>) {
        buildMetricsPreparedAction.set(action)
    }

    /**
     * If `true`, then the metrics will be sent at build finish,
     * orchestrated by Gradle Enterprise plugin, attaching
     * Gradle Build Scan id to metrics.
     *
     * If `false`, then metrics will be sent at build finish by
     * @see [com.automattic.android.measure.lifecycle.BuildFinishedFlowAction].
     */
    val attachGradleScanId: Property<Boolean> = objects.property(Boolean::class.java)
}

package io.github.wzieba.tracks.plugin

import io.github.wzieba.tracks.plugin.analytics.AnalyticsReporter
import io.ktor.utils.io.printStack
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class TracksExtension(
    project: Project,
    private val report: Report,
    private val analyticsReporter: AnalyticsReporter,
    private val authToken: String,
) {

    private val objects = project.objects

    val automatticProject: Property<AutomatticProject> =
        objects.property(AutomatticProject::class.java)

    val enabled: Property<Boolean> = objects.property(Boolean::class.java)

    val obfuscateUsername: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * If `true`, then metrics will be sent at build finish by
     * @see io.github.wzieba.tracks.plugin.analytics.BuildFinishedFlowAction
     *
     * If `false`, then user is required to manually trigger sending metrics,
     * ideally providing Gradle Scan id
     *
     * @see com.gradle.scan.plugin.BuildScanExtension.buildScanPublished
     */
    val sendMetricsOnBuildFinished: Property<Boolean> = objects.property(Boolean::class.java)

    fun reportBuild(gradleScanId: String) {
        return TODO("Not yet implemented")

        if (enabled.orNull != true) return

        val runCatching = kotlin.runCatching {
            runBlocking {
                analyticsReporter.report(report, authToken, gradleScanId)
            }
        }
        println(gradleScanId)

        print(runCatching.exceptionOrNull()?.printStack())
    }

    enum class AutomatticProject {
        WooCommerce, WordPress, DayOne, PocketCasts, Tumblr, FluxC
    }
}

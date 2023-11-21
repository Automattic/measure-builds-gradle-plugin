package io.github.wzieba.tracks.plugin

import org.codehaus.groovy.runtime.EncodingGroovyMethods
import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class TracksExtension constructor(
    private val project: Project,
    private val buildReporter: BuildReporter
) {

    private val objects = project.objects

    val automatticProject: Property<AutomatticProject> = objects.property(AutomatticProject::class.java)

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

    fun reportBuild(gradleScanId: String?) {
        if (enabled.orNull != true) return

        val encodedUser: String = System.getProperty("user.name").let {
            if (obfuscateUsername.getOrElse(false) == true) {
                EncodingGroovyMethods.digest(it, "SHA-1")
            } else {
                it
            }
        }
        val buildData = project.gradle.extensions.getByType(BuildData::class.java)

        buildReporter.report(buildData, encodedUser, gradleScanId)
    }

    enum class AutomatticProject {
        WooCommerce, WordPress, DayOne, PocketCasts, Tumblr, FluxC
    }
}

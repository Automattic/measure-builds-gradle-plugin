package com.automattic.android.measure

import com.automattic.android.measure.lifecycle.BuildFinishedFlowAction
import com.automattic.android.measure.lifecycle.BuildTaskService
import com.automattic.android.measure.networking.MetricsReporter
import com.automattic.android.measure.providers.AuthTokenProvider
import com.automattic.android.measure.providers.BuildDataProvider
import com.automattic.android.measure.providers.UsernameProvider
import com.gradle.scan.plugin.BuildScanExtension
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.flow.FlowActionSpec
import org.gradle.api.flow.FlowProviders
import org.gradle.api.flow.FlowScope
import org.gradle.api.provider.Provider
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.internal.buildevents.BuildStartedTime
import org.gradle.invocation.DefaultGradle
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@Suppress("UnstableApiUsage")
@ExperimentalTime
class BuildTimePlugin @Inject constructor(
    private val registry: BuildEventsListenerRegistry,
    private val flowScope: FlowScope,
    private val flowProviders: FlowProviders,
) : Plugin<Project> {
    override fun apply(project: Project) {
        val start =
            (project.gradle as DefaultGradle).services[BuildStartedTime::class.java].startTime

        val authToken = AuthTokenProvider.provide(project)
        if (authToken.isNullOrBlank()) {
            project.logger.warn("Did not find appsMetricsToken in gradle.properties. Skipping reporting.")
            return
        }

        val analyticsReporter = MetricsReporter(project.logger)

        val extension =
            project.extensions.create("measureBuilds", MeasureBuildsExtension::class.java, project)

        val encodedUser: String = UsernameProvider.provide(project, extension)

        project.afterEvaluate {
            if (extension.enable.orNull == true) {
                InMemoryReport.buildDataStore =
                    BuildDataProvider.provide(
                        project,
                        extension.automatticProject.get(),
                        encodedUser
                    )
                prepareBuildTaskService(project)
                prepareBuildFinishedAction(extension, analyticsReporter, authToken, start)
            }
        }

        prepareBuildScanListener(project, extension, analyticsReporter, authToken)
    }

    private fun prepareBuildScanListener(
        project: Project,
        extension: MeasureBuildsExtension,
        analyticsReporter: MetricsReporter,
        authToken: String,
    ) {
        val buildScanExtension = project.extensions.findByType(BuildScanExtension::class.java)
        buildScanExtension?.buildScanPublished {
            runBlocking {
                if (extension.enable.orNull == true && extension.attachGradleScanId.get()) {
                    analyticsReporter.report(InMemoryReport, authToken, it.buildScanId)
                }
            }
        }
    }

    private fun prepareBuildFinishedAction(
        extension: MeasureBuildsExtension,
        analyticsReporter: MetricsReporter,
        authToken: String?,
        start: Long
    ) {
        flowScope.always(
            BuildFinishedFlowAction::class.java
        ) { spec: FlowActionSpec<BuildFinishedFlowAction.Parameters> ->
            spec.parameters.apply {
                this.buildWorkResult.set(flowProviders.buildWorkResult)
                this.attachGradleScanId.set(extension.attachGradleScanId)
                this.analyticsReporter.set(analyticsReporter)
                this.authToken.set(authToken)
                this.startTime.set(start)
            }
        }
    }

    private fun prepareBuildTaskService(project: Project) {
        val serviceProvider: Provider<BuildTaskService> =
            project.gradle.sharedServices.registerIfAbsent(
                "taskEvents",
                BuildTaskService::class.java
            ) {
            }
        registry.onTaskCompletion(serviceProvider)
    }
}

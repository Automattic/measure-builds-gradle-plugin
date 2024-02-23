package com.automattic.android.measure

import com.automattic.android.measure.lifecycle.BuildFinishedFlowAction
import com.automattic.android.measure.lifecycle.BuildTaskService
import com.automattic.android.measure.lifecycle.ConfigurationPhaseObserver
import com.automattic.android.measure.networking.MetricsReporter
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
        val buildInitiatedTime =
            (project.gradle as DefaultGradle).services[BuildStartedTime::class.java].startTime
        val extension =
            project.extensions.create("measureBuilds", MeasureBuildsExtension::class.java, project)

        val analyticsReporter = MetricsReporter(project.logger, extension.authToken)

        val encodedUser: String = UsernameProvider.provide(project, extension)

        project.afterEvaluate {
            if (extension.enable.orNull == true) {
                val configurationProvider: Provider<Boolean> = project.providers.of(
                    ConfigurationPhaseObserver::class.java
                ) { }
                ConfigurationPhaseObserver.init()

                prepareBuildData(project, extension, encodedUser)
                prepareBuildTaskService(project)
                prepareBuildFinishedAction(extension, analyticsReporter, buildInitiatedTime, configurationProvider)
            }
        }

        prepareBuildScanListener(project, extension, analyticsReporter)
    }

    private fun prepareBuildData(
        project: Project,
        extension: MeasureBuildsExtension,
        encodedUser: String,
    ) {
        InMemoryReport.setBuildData(
            BuildDataProvider.provide(
                project,
                extension.automatticProject.get(),
                encodedUser,
            )
        )
    }

    private fun prepareBuildScanListener(
        project: Project,
        extension: MeasureBuildsExtension,
        analyticsReporter: MetricsReporter,
    ) {
        val buildScanExtension = project.extensions.findByType(BuildScanExtension::class.java)
        buildScanExtension?.buildScanPublished {
            runBlocking {
                if (extension.enable.orNull == true && extension.attachGradleScanId.get()) {
                    analyticsReporter.report(InMemoryReport, it.buildScanId)
                }
            }
        }
    }

    private fun prepareBuildFinishedAction(
        extension: MeasureBuildsExtension,
        analyticsReporter: MetricsReporter,
        buildInitiatedTime: Long,
        configurationPhaseObserver: Provider<Boolean>,
    ) {
        flowScope.always(
            BuildFinishedFlowAction::class.java
        ) { spec: FlowActionSpec<BuildFinishedFlowAction.Parameters> ->
            spec.parameters.apply {
                this.buildWorkResult.set(flowProviders.buildWorkResult)
                this.attachGradleScanId.set(extension.attachGradleScanId)
                this.analyticsReporter.set(analyticsReporter)
                this.initiationTime.set(buildInitiatedTime)
                this.configurationPhaseExecuted.set(configurationPhaseObserver)
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

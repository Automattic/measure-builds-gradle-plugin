@file:Suppress("UnstableApiUsage")

package com.automattic.android.measure.lifecycle

import com.automattic.android.measure.InMemoryReport
import com.automattic.android.measure.analytics.AnalyticsReporter
import com.automattic.android.measure.models.ExecutionData
import kotlinx.coroutines.runBlocking
import org.gradle.api.flow.BuildWorkResult
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import kotlin.jvm.optionals.getOrNull

class BuildFinishedFlowAction : FlowAction<BuildFinishedFlowAction.Parameters> {
    interface Parameters : FlowParameters {
        @get:Input
        val startTime: Property<Long>

        @get:Input
        val buildWorkResult: Property<Provider<BuildWorkResult>>

        @get:Input
        val analyticsReporter: Property<AnalyticsReporter>

        @get:Input
        val authToken: Property<String>

        @get:Input
        val attachGradleScanId: Property<Boolean>

        @get:ServiceReference
        val buildTaskService: Property<BuildTaskService>
    }

    override fun execute(parameters: Parameters) {
        val finish = System.currentTimeMillis()

        val result = parameters.buildWorkResult.get().get()
        val buildTime = finish - parameters.startTime.get()

        val executionData = ExecutionData(
            buildTime = buildTime,
            failed = result.failure.isPresent,
            failure = result.failure.getOrNull(),
            tasks = parameters.buildTaskService.get().tasks,
            buildFinishedTimestamp = finish
        )

        InMemoryReport.executionDataStore = executionData

        if (parameters.attachGradleScanId.get() == false) {
            runBlocking {
                parameters.analyticsReporter.get().report(
                    report = InMemoryReport,
                    authToken = parameters.authToken.get(),
                    gradleScanId = null
                )
            }
        }
    }
}

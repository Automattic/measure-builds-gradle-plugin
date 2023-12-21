@file:Suppress("UnstableApiUsage")

package com.automattic.android.measure.lifecycle

import com.automattic.android.measure.InMemoryReport
import com.automattic.android.measure.models.ExecutionData
import com.automattic.android.measure.networking.MetricsReporter
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
        val initiationTime: Property<Long>

        @get:Input
        val configurationPhaseExecuted: Property<Provider<Boolean>>

        @get:Input
        val buildWorkResult: Property<Provider<BuildWorkResult>>

        @get:Input
        val analyticsReporter: Property<MetricsReporter>

        @get:Input
        val attachGradleScanId: Property<Boolean>

        @get:ServiceReference
        val buildTaskService: Property<BuildTaskService>
    }

    override fun execute(parameters: Parameters) {
        val init = parameters.initiationTime.get()
        val finish = System.currentTimeMillis()

        val result = parameters.buildWorkResult.get().get()
        val buildTime = finish - init

        val configurationTime = if (parameters.configurationPhaseExecuted.get().get()) {
            init - parameters.buildTaskService.get().buildStartTime
        } else {
            0
        }

        val executionData = ExecutionData(
            buildTime = buildTime,
            failed = result.failure.isPresent,
            failure = result.failure.getOrNull(),
            tasks = parameters.buildTaskService.get().tasks,
            buildFinishedTimestamp = finish,
            configurationPhaseDuration = configurationTime
        )

        InMemoryReport.executionDataStore = executionData

        if (parameters.attachGradleScanId.get() == false) {
            runBlocking {
                parameters.analyticsReporter.get().report(
                    report = InMemoryReport,
                    gradleScanId = null
                )
            }
        }
    }
}

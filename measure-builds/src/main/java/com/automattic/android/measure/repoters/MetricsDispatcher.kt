package com.automattic.android.measure.repoters

import com.automattic.android.measure.InMemoryReport
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener

fun interface MetricsReporter: java.io.Serializable {
    suspend fun report(report: InMemoryReport, gradleScanId: String?, parameters: MetricsDispatcher.Parameters)
}

abstract class MetricsDispatcher : BuildService<MetricsDispatcher.Parameters>, OperationCompletionListener {
    interface Parameters : BuildServiceParameters {
        val authToken: Property<String>
        val buildDir: Property<DirectoryProperty>
        val reporters: ListProperty<MetricsReporter>
    }

    suspend fun report(
        report: InMemoryReport,
        gradleScanId: String?
    ) {
        parameters.reporters.get().forEach { it.report(report, gradleScanId, parameters) }
    }


    override fun onFinish(event: FinishEvent?) {
        // Do nothing

        // Without OperationCompletionListener, this service would not be created.
        // https://docs.gradle.org/current/userguide/build_services.html#registering_a_build_service_and_connecting_it_to_tasks
        // This happens on demand when a task first uses the service. If no task uses the service during a build, the service instance will not be created.
    }
}

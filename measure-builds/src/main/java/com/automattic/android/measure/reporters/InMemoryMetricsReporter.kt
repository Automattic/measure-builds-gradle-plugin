package com.automattic.android.measure.reporters

import com.automattic.android.measure.InMemoryReport
import org.gradle.api.Action
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property

object InMemoryMetricsReporter {

    var buildMetricsPreparedAction: Property<Action<MetricsReport>>? = null

    fun report(report: InMemoryReport) {
        val result = object : MetricsReport {
            override val report: InMemoryReport
                get() = report
        }
        if (buildMetricsPreparedAction == null) {
            Logging.getLogger(InMemoryMetricsReporter::class.java).warn(
                "buildMetricsPreparedAction is not set. Metrics will not be reported."
            )
        }
        buildMetricsPreparedAction?.get()?.execute(result)
    }
}

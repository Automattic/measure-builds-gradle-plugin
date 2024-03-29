package com.automattic.android.measure.reporters

import com.automattic.android.measure.InMemoryReport
import org.gradle.api.Action
import org.gradle.api.provider.Property

internal object InMemoryMetricsReporter {

    lateinit var buildMetricsPreparedAction: Property<Action<MetricsReport>>

    fun report(
        report: InMemoryReport,
        gradleScanId: String?
    ) {
        val result = object : MetricsReport {
            override val report: InMemoryReport
                get() = report
            override val gradleScanId: String?
                get() = gradleScanId
        }
        buildMetricsPreparedAction.get().execute(result)
    }
}

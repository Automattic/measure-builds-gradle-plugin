package com.automattic.android.measure.reporters

import com.automattic.android.measure.InMemoryReport

interface MetricsReport {
    val report: InMemoryReport
    val gradleScanId: String?
}

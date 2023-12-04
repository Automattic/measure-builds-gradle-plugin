package com.automattic.android.measure.networking

import com.automattic.android.measure.Report

interface AnalyticsReporter {
    suspend fun report(
        report: Report,
        authToken: String,
        gradleScanId: String?
    )
}

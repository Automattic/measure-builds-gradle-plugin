package io.github.wzieba.tracks.plugin.analytics

import io.github.wzieba.tracks.plugin.Report

interface AnalyticsReporter {
    suspend fun report(
        report: Report,
        authToken: String,
        gradleScanId: String?
    )
}

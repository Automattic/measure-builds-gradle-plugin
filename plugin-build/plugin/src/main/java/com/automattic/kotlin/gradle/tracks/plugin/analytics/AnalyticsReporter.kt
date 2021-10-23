package com.automattic.kotlin.gradle.tracks.plugin.analytics

import com.automattic.kotlin.gradle.tracks.plugin.BuildData

interface AnalyticsReporter {
    suspend fun report(event: BuildData, username: String, debug: Boolean)
}

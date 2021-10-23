package com.automattic.kotlin.gradle.tracks.plugin.analytics

import com.automattic.kotlin.gradle.tracks.plugin.BuildData

class ConsoleReporter(private val reporterName: String) : AnalyticsReporter {

    override suspend fun report(event: BuildData) {
        println("$reporterName: $event")
    }
}

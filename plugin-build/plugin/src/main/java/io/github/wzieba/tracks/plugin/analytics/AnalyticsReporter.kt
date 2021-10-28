package io.github.wzieba.tracks.plugin.analytics

import io.github.wzieba.tracks.plugin.BuildData

interface AnalyticsReporter {
    suspend fun report(event: BuildData, username: String?, customEventName: String?, debug: Boolean)
}

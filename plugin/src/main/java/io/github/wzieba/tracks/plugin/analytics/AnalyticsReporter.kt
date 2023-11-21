package io.github.wzieba.tracks.plugin.analytics

import io.github.wzieba.tracks.plugin.BuildData
import org.gradle.api.logging.Logger

interface AnalyticsReporter {
    suspend fun report(
        logger: Logger,
        event: BuildData,
        user: String,
        gradleScanId: String?
    )
}

package com.ncorti.kotlin.gradle.template.plugin.analytics

import com.ncorti.kotlin.gradle.template.plugin.BuildData

interface AnalyticsReporter {
    suspend fun report(event: BuildData)
}

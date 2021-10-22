package com.ncorti.kotlin.gradle.template.plugin.analytics

import com.ncorti.kotlin.gradle.template.plugin.BuildData

class ConsoleReporter(private val reporterName: String) : AnalyticsReporter {

  override suspend fun report(event: BuildData) {
      println("$reporterName: $event")
  }
}
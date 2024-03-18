package com.automattic.android.measure.repoters

import com.automattic.android.measure.InMemoryReport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.logging.Logging
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writeText

object LocalMetricsReporter : MetricsReporter {
    private val logger = Logging.getLogger(LocalMetricsReporter::class.java)
    override suspend fun report(
        report: InMemoryReport,
        gradleScanId: String?,
        parameters: MetricsDispatcher.Parameters
    ) {
        Path("${parameters.buildDir.get().asFile.get().path}/reports/measure_builds")
            .apply {
                if (!exists()) {
                    createDirectories()
                }
                resolve("build_data.json").apply {
                    logger.info("Writing build data to ${absolutePathString()}")
                    if (!exists()) {
                        createFile()
                    }
                    writeText(Json.encodeToString(report.buildData))
                }
                resolve("execution_data.json").apply {
                    logger.info("Writing execution data to ${absolutePathString()}")
                    if (!exists()) {
                        createFile()
                    }
                    writeText(Json.encodeToString(report.executionData))
                }
            }
    }
}
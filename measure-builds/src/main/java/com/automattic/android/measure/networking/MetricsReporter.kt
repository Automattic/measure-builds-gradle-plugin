package com.automattic.android.measure.networking

import com.automattic.android.measure.InMemoryReport
import com.automattic.android.measure.logging.Emojis.FAILURE_ICON
import com.automattic.android.measure.logging.Emojis.SUCCESS_ICON
import com.automattic.android.measure.logging.Emojis.TURTLE_ICON
import com.automattic.android.measure.logging.Emojis.WAITING_ICON
import com.automattic.android.measure.models.MeasuredTask
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds

class MetricsReporter(
    private val logger: Logger,
    private val authToken: Provider<String>,
    private val buildDir: File,
) {
    suspend fun report(
        report: InMemoryReport,
        gradleScanId: String?
    ) {
        reportLocally(report)

        val payload = report.toAppsInfraPayload(gradleScanId)
        @Suppress("TooGenericExceptionCaught")
        try {
            logSlowTasks(report)
            if (!authToken.isPresent) {
                logger.lifecycle("\nNo authToken provided. Skipping reporting.")
                return
            }
            logger.lifecycle("\n$WAITING_ICON Reporting build data to Apps Metrics...")

            val client = httpClient()

            client.post<HttpStatement>("https://metrics.a8c-ci.services/api/grouped-metrics") {
                headers {
                    append(HttpHeaders.UserAgent, "Gradle")
                    append(Authorization, "Bearer ${authToken.get()}")
                }
                contentType(ContentType.Application.Json)
                body = payload
            }.execute { response: HttpResponse ->
                logger.debug(response.toString())

                when (response.status) {
                    HttpStatusCode.Created -> {
                        val buildTime = report.executionData.buildTime
                        val timeFormatted = String.format(
                            Locale.US,
                            "%dm %ds",
                            MILLISECONDS.toMinutes(buildTime),
                            MILLISECONDS.toSeconds(buildTime) - MINUTES.toSeconds(
                                MILLISECONDS.toMinutes(
                                    buildTime
                                )
                            )
                        )
                        logger.lifecycle(
                            "\n$SUCCESS_ICON Build time report of $timeFormatted has been received by Apps Metrics."
                        )
                    }

                    else -> {
                        logger.warn(
                            "\n$FAILURE_ICON Build time has not been uploaded. Add `debug` property to see more logs."
                        )
                    }
                }
            }
            client.close()
        } catch (exception: Exception) {
            logger.warn(
                "\n$FAILURE_ICON Build time has not been uploaded. Add `debug` property to see more logs."
            )
            logger.debug(exception.stackTraceToString())
        }
    }

    private fun reportLocally(report: InMemoryReport) {
        Path("${buildDir.path}/reports/measure_builds")
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

    private fun httpClient(): HttpClient {
        val client = HttpClient(CIO) {
            install(Logging) {
                this.logger = object : io.ktor.client.features.logging.Logger {
                    override fun log(message: String) {
                        this@MetricsReporter.logger.debug(message)
                    }
                }
                level = LogLevel.ALL
            }
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5.seconds.inWholeMilliseconds
            }
        }
        return client
    }

    private fun logSlowTasks(report: InMemoryReport) {
        val slowTasks =
            report.executionData.tasks.sortedByDescending { it.duration }.chunked(atMostLoggedTasks)
                .first()
        logger.lifecycle("\n$TURTLE_ICON ${slowTasks.size} slowest tasks were: ")

        logger.lifecycle(
            String.format(
                Locale.US,
                "%-15s %-15s %s",
                "Duration",
                "% of build",
                "Task"
            )
        )
        slowTasks.forEach {
            @Suppress("MagicNumber")
            logger.lifecycle(
                "%-15s %-15s %s".format(
                    Locale.US,
                    readableDuration(it),
                    "${(it.duration.inWholeMilliseconds * 100 / report.executionData.buildTime).toInt()}%",
                    it.name,
                )
            )
        }
    }

    private fun readableDuration(it: MeasuredTask) =
        if (it.duration < 1.seconds) {
            "${it.duration.inWholeMilliseconds}ms"
        } else {
            "${it.duration.inWholeSeconds}s"
        }

    companion object {
        private const val atMostLoggedTasks = 5
    }
}

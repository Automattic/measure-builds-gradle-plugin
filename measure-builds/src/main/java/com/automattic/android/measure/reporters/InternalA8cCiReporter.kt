package com.automattic.android.measure.reporters

import com.automattic.android.measure.logging.Emojis.FAILURE_ICON
import com.automattic.android.measure.logging.Emojis.SUCCESS_ICON
import com.automattic.android.measure.logging.Emojis.WAITING_ICON
import com.automattic.android.measure.networking.toAppsMetricsPayload
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.time.Duration.Companion.seconds
import org.gradle.api.logging.Logging as GradleLogging

object InternalA8cCiReporter {
    private val logger =
        GradleLogging.getLogger(InternalA8cCiReporter::class.java)

    @JvmStatic
    fun reportBlocking(
        metricsReport: MetricsReport,
        projectName: String,
        authToken: String?,
    ) {
        runBlocking {
            report(metricsReport, projectName, authToken)
        }
    }

    suspend fun report(
        metricsReport: MetricsReport,
        projectName: String,
        authToken: String?,
    ) {
        val report = metricsReport.report
        val payload = report.toAppsMetricsPayload(projectName, metricsReport.gradleScanId)
        @Suppress("TooGenericExceptionCaught")
        try {
            if (authToken.isNullOrBlank()) {
                logger.lifecycle("\nNo authToken provided. Skipping reporting.")
                return
            }
            logger.lifecycle("\n$WAITING_ICON Reporting build data to Apps Metrics...")

            val client = httpClient()

            val response = client.post(Url("https://metrics.a8c-ci.services/api/grouped-metrics")) {
                headers {
                    append(HttpHeaders.UserAgent, "Gradle")
                    append(Authorization, "Bearer $authToken")
                }
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

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
        } catch (exception: Exception) {
            logger.warn(
                "\n$FAILURE_ICON Build time has not been uploaded. Add `debug` property to see more logs."
            )
            logger.debug(exception.stackTraceToString())
        }
    }

    private fun httpClient(): HttpClient {
        val client = HttpClient(CIO) {
            install(Logging) {
                this.logger = object : Logger {
                    override fun log(message: String) {
                        this@InternalA8cCiReporter.logger.debug(message)
                    }
                }
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                    }
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5.seconds.inWholeMilliseconds
            }
        }
        return client
    }
}

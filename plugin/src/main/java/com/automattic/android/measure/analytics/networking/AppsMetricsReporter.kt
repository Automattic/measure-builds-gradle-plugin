package com.automattic.android.measure.analytics.networking

import com.automattic.android.measure.Report
import com.automattic.android.measure.analytics.AnalyticsReporter
import com.automattic.android.measure.analytics.Emojis.FAILURE_ICON
import com.automattic.android.measure.analytics.Emojis.TURTLE_ICON
import com.automattic.android.measure.analytics.Emojis.SUCCESS_ICON
import com.automattic.android.measure.analytics.Emojis.WAITING_ICON
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
import org.gradle.api.logging.Logger
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.time.Duration.Companion.seconds

class AppsMetricsReporter(
    private val logger: Logger,
) : AnalyticsReporter {

    override suspend fun report(
        report: Report,
        authToken: String,
        gradleScanId: String?
    ) {
        @Suppress("TooGenericExceptionCaught")
        try {
            logger.debug("Reporting $report")
            logSlowTasks(report)
            logger.lifecycle("\n$WAITING_ICON Reporting build data to Apps Metrics...")

            val client = httpClient()

            client.post<HttpStatement>("https://seal-app-e8plp.ondigitalocean.app/api/grouped-metrics") {
                headers {
                    append(HttpHeaders.UserAgent, "Gradle")
                    append(Authorization, "Bearer $authToken")
                }
                contentType(ContentType.Application.Json)
                body = report.toAppsInfraPayload(gradleScanId)
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
                            "\n$SUCCESS_ICON Build time report of $timeFormatted has been received by App Metrics."
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

    private fun httpClient(): HttpClient {
        val client = HttpClient(CIO) {
            install(Logging) {
                this.logger = object : io.ktor.client.features.logging.Logger {
                    override fun log(message: String) {
                        this@AppsMetricsReporter.logger.debug(message)
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

    private fun logSlowTasks(report: Report) {
        val slowTasks =
            report.executionData.tasks.sortedByDescending { it.duration }.chunked(atMostLoggedTasks)
                .first()
        logger.lifecycle("\n$TURTLE_ICON ${slowTasks.size} slowest tasks were: ")
        slowTasks.forEach {
            @Suppress("MagicNumber")
            logger.lifecycle(
                "${it.name} " +
                    "(${(it.duration.inWholeMilliseconds * 100 / report.executionData.buildTime).toInt()}%" +
                    "of build)"
            )
        }
    }

    companion object {
        private const val atMostLoggedTasks = 5
    }
}

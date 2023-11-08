package io.github.wzieba.tracks.plugin.analytics.networking

import io.github.wzieba.tracks.plugin.BuildData
import io.github.wzieba.tracks.plugin.analytics.AnalyticsReporter
import io.github.wzieba.tracks.plugin.analytics.Emojis.FAILURE_ICON
import io.github.wzieba.tracks.plugin.analytics.Emojis.SUCCESS_ICON
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.timeout
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.time.Duration.Companion.seconds

class AppsMetricsReporter(private val project: Project) : AnalyticsReporter {

    override suspend fun report(
        logger: Logger,
        event: BuildData,
        user: String,
    ) {
        val authToken: String? = project.properties["appsMetricsToken"] as String?

        if (authToken.isNullOrBlank()) {
            logger.warn("Did not find appsMetricsToken in gradle.properties. Skipping reporting.")
            return
        }

        logger.debug("Reporting $event")

        val client = HttpClient(CIO) {
            install(Logging) {
                this.logger = object : io.ktor.client.features.logging.Logger {
                    override fun log(message: String) {
                        logger.debug(message)
                    }
                }
                level = io.ktor.client.features.logging.LogLevel.ALL
            }
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5.seconds.inWholeMilliseconds
            }
        }

        client.post<HttpStatement>("https://seal-app-e8plp.ondigitalocean.app/api/grouped-metrics") {
            headers {
                append(HttpHeaders.UserAgent, "Gradle")
                append(Authorization, "Bearer $authToken")
            }
            contentType(ContentType.Application.Json)
            body = event.toAppsInfraPayload(user)
        }.execute { response: HttpResponse ->
            logger.debug(response.toString())

            when (response.status) {
                HttpStatusCode.Created -> {
                    val buildTime = event.buildTime
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
                        "$SUCCESS_ICON Build time report of $timeFormatted has been received by App Metrics."
                    )
                }

                else -> {
                    logger.warn(
                        "$FAILURE_ICON Build time has not been uploaded. Add `debug` property to see more logs."
                    )
                }
            }
        }
        client.close()
    }
}

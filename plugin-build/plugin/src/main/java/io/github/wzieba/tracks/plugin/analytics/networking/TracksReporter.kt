package io.github.wzieba.tracks.plugin.analytics.networking

import io.github.wzieba.tracks.plugin.BuildData
import io.github.wzieba.tracks.plugin.analytics.AnalyticsReporter
import io.github.wzieba.tracks.plugin.analytics.Emojis.FAILURE_ICON
import io.github.wzieba.tracks.plugin.analytics.Emojis.SUCCESS_ICON
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.gradle.api.logging.Logger
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES

class TracksReporter : AnalyticsReporter {

    override suspend fun report(
        logger: Logger,
        event: BuildData,
        username: String?,
        customEventName: String?,
    ) {
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
        }

        client.post<HttpStatement>("https://public-api.wordpress.com/rest/v1.1/tracks/record") {
            headers {
                append(HttpHeaders.UserAgent, "Gradle")
            }
            contentType(ContentType.Application.Json)
            body = event.toTracksPayload(customEventName, username)
        }.execute { response: HttpResponse ->
            logger.debug(response.toString())

            when (response.status) {
                HttpStatusCode.Accepted -> {
                    val buildTime = event.buildTime
                    val timeFormatted = String.format(
                        Locale.US,
                        "%dm %ds",
                        MILLISECONDS.toMinutes(buildTime),
                        MILLISECONDS.toSeconds(buildTime) - MINUTES.toSeconds(MILLISECONDS.toMinutes(buildTime))
                    )
                    logger.lifecycle("$SUCCESS_ICON Build time report of $timeFormatted has been received by Tracks.")
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

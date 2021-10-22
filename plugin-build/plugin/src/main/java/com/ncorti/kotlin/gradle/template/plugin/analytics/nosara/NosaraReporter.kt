package com.ncorti.kotlin.gradle.template.plugin.analytics.nosara

import com.ncorti.kotlin.gradle.template.plugin.BuildData
import com.ncorti.kotlin.gradle.template.plugin.analytics.AnalyticsReporter
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class NosaraReporter : AnalyticsReporter {

    override suspend fun report(event: BuildData) {
        val client = HttpClient(CIO) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }

        client.post<Unit>("https://public-api.wordpress.com/rest/v1.1/tracks/record") {
            headers {
                append(HttpHeaders.UserAgent, "Gradle")
            }
            contentType(ContentType.Application.Json)
            body = event.toNosaraPayload()
        }

        client.close()
    }
}

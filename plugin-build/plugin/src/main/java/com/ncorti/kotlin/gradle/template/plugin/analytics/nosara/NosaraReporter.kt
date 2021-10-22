package com.ncorti.kotlin.gradle.template.plugin.analytics.nosara

import com.ncorti.kotlin.gradle.template.plugin.BuildData
import com.ncorti.kotlin.gradle.template.plugin.analytics.AnalyticsReporter
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*


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


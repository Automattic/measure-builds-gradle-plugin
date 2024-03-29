package com.automattic.android.measure

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class GroovyConfigurationTest {

    @Test
    fun `given a project configured via groovy, when running the build, the build succeeds and buildMetricsPrepared action is executed`() {
        // given
        // language=groovy
        val groovyBuildGradle =
            """
            import com.automattic.android.measure.reporters.InternalA8cCiReporter
            import com.automattic.android.measure.reporters.LocalMetricsReporter
            import com.automattic.android.measure.reporters.SlowSlowTasksMetricsReporter 
            import com.automattic.android.measure.reporters.MetricsReport

            plugins {
                id("com.automattic.android.measure-builds")
            }
            
            measureBuilds {
                enable = true
                attachGradleScanId = false
                onBuildMetricsReadyListener { MetricsReport report -> 
                    SlowSlowTasksMetricsReporter.report(report)
                    LocalMetricsReporter.report(report, buildDir.absolutePath)
                }
            }
            """
        val projectDir = File("build/functionalTest").apply {
            mkdirs()
            resolve("settings.gradle").writeText("")
            resolve("build.gradle").writeText(
                groovyBuildGradle.trimIndent()
            )
        }

        // when
        val result = GradleRunner
            .create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("help", "--stacktrace")
            .withProjectDir(projectDir)
            .build()

        // then
        assertThat(result.output).contains("BUILD SUCCESSFUL")
    }
}

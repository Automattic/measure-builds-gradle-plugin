package com.automattic.android.measure

import com.automattic.android.measure.models.ExecutionData
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Test suite focused on validating the behavior of the Build Time Plugin when Isolated Projects is enabled.
 * See https://docs.gradle.org/current/userguide/isolated_projects.html
 */
@Suppress("MaximumLineLength", "MaxLineLength")
class BuildTimePluginIsolatedProjectsTest {

    @BeforeEach
    fun clear() {
        File("build/functionalTest").deleteRecursively()
    }

    @Test
    fun `given a multi-project build with isolated projects, when plugin is applied to root project, then build succeeds and metrics are reported`() {
        // given
        val runner = runner(applyPluginTo = ":")

        // when
        val result = runner.build()

        // then
        assertThat(result.output).contains("Isolated projects is an incubating feature")
        assertThat(executionData.requestedTasks).contains("help")
    }

    @Test
    fun `given a multi-project build with isolated projects, when plugin is applied to a subproject, then build succeeds and metrics are reported`() {
        // given
        val runner = runner(applyPluginTo = ":app")

        // when
        val result = runner.build()

        // then
        assertThat(result.output).contains("Isolated projects is an incubating feature")
        assertThat(executionData.requestedTasks).contains("help")
    }

    private fun runner(applyPluginTo: String): GradleRunner {
        val projectDir = File("build/functionalTest").apply {
            mkdirs()
            resolve("settings.gradle.kts").writeText(
                """
                rootProject.name = "isolated"
                include(":app")
                include(":lib")
                """.trimIndent()
            )
            resolve("app").mkdirs()
            resolve("lib").mkdirs()
            resolve("lib/build.gradle.kts").writeText("")

            val pluginBuildScript = """
                plugins {
                    id("com.automattic.android.measure-builds")
                }
                val buildPathProperty = project.layout.buildDirectory.map { it.asFile.path }
                measureBuilds {
                    enable.set(true)
                    attachGradleScanId.set(false)
                    onBuildMetricsReadyListener {
                        val buildPath = buildPathProperty.get()
                        com.automattic.android.measure.reporters.LocalMetricsReporter.report(this, buildPath)
                        com.automattic.android.measure.reporters.SlowSlowTasksMetricsReporter.report(this)
                    }
                }
            """.trimIndent()

            if (applyPluginTo == ":") {
                resolve("build.gradle.kts").writeText(pluginBuildScript)
                resolve("app/build.gradle.kts").writeText("")
            } else {
                resolve("build.gradle.kts").writeText("")
                resolve("app/build.gradle.kts").writeText(pluginBuildScript)
            }
        }

        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments(
                "help",
                "--configuration-cache",
                "-Dorg.gradle.unsafe.isolated-projects=true",
                "--stacktrace",
            )
            .withProjectDir(projectDir)
    }

    private val executionData: ExecutionData
        get() {
            val reportDir = File("build/functionalTest").walkTopDown()
                .first { it.isDirectory && it.name == "measure_builds" }
            return Json.decodeFromString<ExecutionData>(reportDir.resolve("execution_data.json").readText())
        }
}

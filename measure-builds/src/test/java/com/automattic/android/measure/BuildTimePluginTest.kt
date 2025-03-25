package com.automattic.android.measure

import com.automattic.android.measure.models.ExecutionData
import com.automattic.android.measure.models.MeasuredTask
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@Suppress("MaximumLineLength", "MaxLineLength")
class BuildTimePluginTest {

    @Test
    fun `given a project that disabled build measurements, when executing a task, then build metrics are not sent`() {
        // given
        val runner = functionalTestRunner(enable = false)

        // when
        val run = runner.withArguments("help").build()

        // then
        assertThat(run.output).doesNotContain("Reporting build data to Apps Metrics...")
    }

    @Test
    fun `given a project that did not enable build measurements, when executing a task, then build metrics are not sent`() {
        // given
        val runner = functionalTestRunner(enable = null)

        // when
        val run = runner.withArguments("help").build()

        // then
        assertThat(run.output).doesNotContain("Reporting build data to Apps Metrics...")
    }

    @Test
    fun `given a project without apps metrics token, when executing a task, then build does not fail`() {
        // given
        val runner = functionalTestRunner(
            enable = true,
            applyAppsMetricsToken = false,
        )

        // when
        val run = runner.withArguments("help").build()

        // then
        assertThat(run.output).contains("No authToken provided. Skipping reporting.")
            .contains("BUILD SUCCESSFUL")
    }

    @Test
    fun `given a help task to execute, when finishing the build, the help task is present in report file`() {
        // given
        val runner = functionalTestRunner(
            enable = true,
            applyAppsMetricsToken = false,
        )

        // when
        runner.withArguments("help").build()

        // then
        File("build/functionalTest/build/reports/measure_builds/execution_data.json").let {
            val executionData = Json.decodeFromString<ExecutionData>(it.readText())

            assertThat(executionData.executedTasks).hasSize(1).first().satisfies({ task ->
                assertThat(task.name).isEqualTo(":help")
                assertThat(task.state).isEqualTo(MeasuredTask.State.EXECUTED)
            })
        }
    }

    @BeforeEach
    fun clearCache() {
        val projectDir = File("build/functionalTest")
        projectDir.deleteRecursively()
    }

    private fun functionalTestRunner(
        enable: Boolean?,
        applyAppsMetricsToken: Boolean = true,
        vararg arguments: String,
    ): GradleRunner {
        val projectDir = File("build/functionalTest").apply {
            mkdirs()
            resolve("settings.gradle.kts").writeText("")
            resolve("build.gradle.kts").writeText(
                """
                     plugins {
                         id("com.automattic.android.measure-builds")
                     }
                     val buildPathProperty = project.layout.buildDirectory.map { it.asFile.path }
                     measureBuilds {
                         ${if (enable != null) "enable.set($enable)" else ""}
                         onBuildMetricsReadyListener {
                              val buildPath = buildPathProperty.get()
                              com.automattic.android.measure.reporters.LocalMetricsReporter.report(this, buildPath)
                              com.automattic.android.measure.reporters.SlowSlowTasksMetricsReporter.report(this)
                              com.automattic.android.measure.reporters.InternalA8cCiReporter.reportBlocking(this, "woocommerce", ${if (applyAppsMetricsToken) "\"token\"" else "\"\""})
                         }
                     }
                """.trimIndent()
            )
        }

        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments(arguments.toList())
            .withProjectDir(projectDir)
    }
}

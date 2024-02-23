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
    fun `given a project that attaches gradle scan id, when executing a task with configuration from cache, then send the report with attached gradle scan id`() {
        // given
        val runner = functionalTestRunner(
            enable = true,
            attachGradleScanId = true,
            projectWithSendingScans = true
        )

        // when
        val prepareConfigurationCache =
            runner.withArguments("--configuration-cache", "help").build()

        // then
        assertThat(
            prepareConfigurationCache.output
        ).contains("Calculating task graph as no configuration cache is available for tasks")
            .contains("Configuration cache entry stored")

        // when
        val buildUsingConfigurationCache =
            runner.withArguments("--configuration-cache", "help", "--debug").build()

        // then
        assertThat(buildUsingConfigurationCache.output).contains("Reusing configuration cache")
            .contains("Reporting build data to Apps Metrics...")
            .contains("{\"name\":\"woocommerce-gradle-scan-id\",\"value\":")
            .doesNotContain("{\"name\":\"woocommerce-gradle-scan-id\",\"value\":\"null\"}")
    }

    @Test
    fun `given a project that disabled build measurements and does not attach Gradle Scan Id, when executing a task, then build metrics are not sent`() {
        // given
        val runner = functionalTestRunner(
            enable = false,
            attachGradleScanId = false,
        )

        // when
        val run = runner.withArguments("help").build()

        // then
        assertThat(run.output).doesNotContain("Reporting build data to Apps Metrics...")
    }

    @Test
    fun `given a project that did not enable build measurements and does not attach Gradle Scan Id, when executing a task, then build metrics are not sent`() {
        // given
        val runner = functionalTestRunner(
            enable = null,
            attachGradleScanId = false,
        )

        // when
        val run = runner.withArguments("help").build()

        // then
        assertThat(run.output).doesNotContain("Reporting build data to Apps Metrics...")
    }

    @Test
    fun `given a project that disabled build measurements and does attach Gradle Scan Id, when executing a task, then build metrics are not sent`() {
        // given
        val runner = functionalTestRunner(
            enable = false,
            attachGradleScanId = true,
        )

        // when
        val run = runner.withArguments("help").build()

        // then
        assertThat(run.output).doesNotContain("Reporting build data to Apps Metrics...")
    }

    @Test
    fun `given a project that did not enable build measurements and does attach Gradle Scan Id, when executing a task, then build metrics are not sent`() {
        // given
        val runner = functionalTestRunner(
            enable = null,
            attachGradleScanId = true,
        )

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
            attachGradleScanId = false,
            applyAppsMetricsToken = false,
        )

        // when
        val run = runner.withArguments("help").build()

        // then
        assertThat(run.output)
            .contains("No authToken provided. Skipping reporting.")
            .contains("BUILD SUCCESSFUL")
    }

    @Test
    fun `given a help task to execute, when finishing the build, the help task is present in report file`() {
        // given
        val runner = functionalTestRunner(
            enable = true,
            attachGradleScanId = false,
            applyAppsMetricsToken = false,
        )

        // when
        val run = runner.withArguments("help").build()

        // then
        File("build/reports/measure_builds/execution_data.json").let {
            val executionData = Json.decodeFromString<ExecutionData>(it.readText())

            assertThat(executionData.tasks).hasSize(1)
                .first().satisfies({ task ->
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
        projectWithSendingScans: Boolean = false,
        attachGradleScanId: Boolean,
        applyAppsMetricsToken: Boolean = true,
        vararg arguments: String,
    ): GradleRunner {
        val projectDir = File("build/functionalTest")
        projectDir.mkdirs()
        if (projectWithSendingScans) {
            projectDir.resolve("settings.gradle.kts").writeText(
                """
            plugins {
                id("com.gradle.enterprise") version "3.15.1"
            }
            gradleEnterprise {
                buildScan {
                    publishAlways()
                    termsOfServiceUrl = "https://gradle.com/terms-of-service"
                    termsOfServiceAgree = "yes"
                    isUploadInBackground = false
                }
            }
                """.trimIndent()
            )
        }
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.automattic.android.measure-builds")
            }
            measureBuilds {
                ${if (enable != null) "enable.set($enable)" else ""}
                attachGradleScanId.set($attachGradleScanId)
                automatticProject.set(com.automattic.android.measure.MeasureBuildsExtension.AutomatticProject.WooCommerce)
                ${if (applyAppsMetricsToken) "authToken.set(\"token\")" else ""}
            }
            """.trimIndent()
        )

        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(arguments.toList())
        runner.withProjectDir(projectDir)
        return runner
    }
}

package com.automattic.android.measure

import com.automattic.android.measure.models.BuildData
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
        assertThat(run.output).contains("No authToken provided. Skipping reporting.")
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

    @Test
    fun `given a help task to execute and using obfuscateUsername, when finishing the build, the user name is obfuscated`() {
        // given
        val runner = functionalTestRunner(
            enable = true,
            attachGradleScanId = false,
            applyAppsMetricsToken = false,
            obfuscateUsername = true,
        )

        // when
        runner.withArguments("help").build()

        // then
        File("build/functionalTest/build/reports/measure_builds/build_data.json").let {
            val buildData = Json.decodeFromString<BuildData>(it.readText())

            assertThat(buildData.user).hasSize(40)
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
        obfuscateUsername: Boolean = false,
        vararg arguments: String,
    ): GradleRunner {
        val projectDir = File("build/functionalTest").apply {
            mkdirs()
            if (projectWithSendingScans) {
                resolve("settings.gradle.kts").writeText(
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
            resolve("build.gradle.kts").writeText(
                """
                     plugins {
                         id("com.automattic.android.measure-builds")
                     }
                     val buildPathProperty = project.layout.buildDirectory.map { it.asFile.path }
                     measureBuilds {
                         ${if (enable != null) "enable.set($enable)" else ""}
                         attachGradleScanId.set($attachGradleScanId)
                         obfuscateUsername.set($obfuscateUsername)
                         buildMetricsPrepared{
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

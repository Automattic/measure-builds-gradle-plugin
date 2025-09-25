package com.automattic.android.measure.providers

class MachineDataProvider {

    fun operatingSystem(): String {
        return System.getProperty("os.name").lowercase()
    }
    fun architecture(): String {
        val operatingSystem = this.operatingSystem()
        var architecture = "unknown"

        if (operatingSystem.contains("mac")) {
            architecture = runCliCommand(listOf("uname", "-m"))
        }

        if (operatingSystem.contains("linux")) {
            architecture = runCliCommand(listOf("uname", "-m"))
        }

        if (operatingSystem.contains("win")) {
            architecture = runCliCommand(listOf("echo", "%PROCESSOR_ARCHITECTURE%"))
        }

        return architecture
    }

    private fun runCliCommand(command: List<String>): String {
        val process = ProcessBuilder().apply {
            command(command)
        }.start()

        process.waitFor()

        return process.inputStream.reader().readText().trim()
    }
}
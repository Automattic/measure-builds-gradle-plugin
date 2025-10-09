package com.automattic.android.measure.providers

class MachineDataProvider {

    fun operatingSystem(): String {
        return System.getProperty("os.name").lowercase()
    }

    fun architecture(): String {
        // Use os.arch system property which is:
        // - Cross-platform (works on Mac, Linux, Windows)
        // - Configuration-cache safe (no external process calls)
        // - Works on JDK 21+ (avoids security restrictions on exec)
        // Returns values like: aarch64, x86_64, amd64, arm64, etc.
        return System.getProperty("os.arch")
    }
}

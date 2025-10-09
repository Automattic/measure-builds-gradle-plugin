package com.automattic.android.measure

import com.automattic.android.measure.providers.MachineDataProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class MachineDataProviderTest {

    @Test
    fun testArchitectureLookup() {
        val architecture = MachineDataProvider().architecture()
        val os = System.getProperty("os.name").lowercase()

        // Verify we get a non-empty value
        assert(architecture.isNotEmpty()) { "Architecture should not be empty" }

        // Verify it's a known architecture format
        val knownArchitectures = setOf(
            "aarch64", "arm64",  // ARM 64-bit
            "x86_64", "amd64",   // x86 64-bit
            "x86", "i386"         // x86 32-bit
        )
        assert(architecture in knownArchitectures) {
            "Unknown architecture: $architecture on OS: $os"
        }
    }
}

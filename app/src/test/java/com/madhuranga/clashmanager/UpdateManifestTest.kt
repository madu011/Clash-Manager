package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UpdateManifestTest {
    private val hash = "a".repeat(64)

    private fun json(
        apkUrl: String = "https://github.com/madu011/Clash-Manager/releases/download/v1.1.0/app-release.apk",
        dataUrl: String = "https://raw.githubusercontent.com/madu011/Clash-Manager/main/updates/game-data.json",
        apkHash: String = hash
    ) = """{
        "latestVersionCode": 2,
        "latestVersionName": "1.1.0",
        "apkUrl": "$apkUrl",
        "apkSha256": "$apkHash",
        "minimumSupportedVersionCode": 1,
        "dataPackVersion": 3,
        "dataPackUrl": "$dataUrl",
        "dataPackSha256": "$hash"
    }"""

    @Test fun parsesTrustedManifest() {
        val parsed = UpdateManifest.parse(json())
        assertEquals(2, parsed.latestVersionCode)
        assertEquals(3, parsed.dataPackVersion)
    }

    @Test fun rejectsNonHttpsApk() {
        assertThrows(IllegalArgumentException::class.java) {
            UpdateManifest.parse(json(apkUrl = "http://github.com/madu011/Clash-Manager/releases/download/v1/app.apk"))
        }
    }

    @Test fun rejectsUntrustedHost() {
        assertThrows(IllegalArgumentException::class.java) {
            UpdateManifest.parse(json(dataUrl = "https://example.com/game-data.json"))
        }
    }

    @Test fun rejectsInvalidChecksum() {
        assertThrows(IllegalArgumentException::class.java) {
            UpdateManifest.parse(json(apkHash = "not-a-checksum"))
        }
    }
}

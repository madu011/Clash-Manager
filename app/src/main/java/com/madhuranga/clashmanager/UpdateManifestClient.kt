package com.madhuranga.clashmanager

import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches the release manifest from the repository's main branch.
 * A missing manifest is reported as an unavailable release, not treated as an update.
 */
class UpdateManifestClient(
    private val manifestUrl: String = "https://raw.githubusercontent.com/madu011/Clash-Manager/main/updates/manifest.json",
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    }
) {
    fun fetch(): UpdateManifest {
        val connection = connectionFactory(URL(manifestUrl))
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                error("No release manifest has been published yet")
            }
            require(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "Update check failed (HTTP ${connection.responseCode})"
            }
            val length = connection.contentLengthLong
            require(length in 1..64 * 1024L) { "Update manifest size is invalid" }
            val raw = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            require(raw.toByteArray(Charsets.UTF_8).size <= 64 * 1024) { "Update manifest is too large" }
            return UpdateManifest.parse(raw)
        } finally {
            connection.disconnect()
        }
    }
}

package com.madhuranga.clashmanager

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest

/**
 * Downloads a versioned game-data JSON pack into app-private storage.
 * A pack becomes active only after HTTPS host/path, size, checksum and JSON
 * structure checks pass. The previous valid pack is preserved on failure.
 */
class GameDataPackInstaller(
    context: Context,
    private val maxBytes: Int = 5 * 1024 * 1024,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    }
) {
    private val directory = File(context.filesDir, "game-data").apply { mkdirs() }
    private val activeFile = File(directory, "active-pack.json")
    private val tempFile = File(directory, "download-pack.tmp")

    fun install(manifest: UpdateManifest): Result<File> = runCatching {
        val uri = URI(manifest.dataPackUrl)
        require(uri.scheme == "https" && uri.host == "raw.githubusercontent.com")
        require(uri.path.startsWith("/madu011/Clash-Manager/") && !uri.path.contains(".."))

        val connection = connectionFactory(URL(manifest.dataPackUrl))
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")
            connection.connect()
            require(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "Game data download failed (HTTP ${connection.responseCode})"
            }
            val length = connection.contentLengthLong
            require(length in 1..maxBytes.toLong()) { "Game data file size is invalid or too large" }

            val digest = MessageDigest.getInstance("SHA-256")
            var total = 0
            connection.inputStream.use { input ->
                tempFile.outputStream().buffered().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= maxBytes) { "Game data file exceeds the size limit" }
                        digest.update(buffer, 0, read)
                        output.write(buffer, 0, read)
                    }
                }
            }
            require(total > 0) { "Game data file is empty" }
            val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
            require(actualHash.equals(manifest.dataPackSha256, ignoreCase = true)) {
                "Game data checksum does not match"
            }

            val json = JSONObject(tempFile.readText(Charsets.UTF_8))
            require(json.getInt("schemaVersion") == 1) { "Unsupported game data schema" }
            require(json.getInt("dataPackVersion") == manifest.dataPackVersion) {
                "Game data version does not match the update manifest"
            }
            require(json.getJSONArray("items").length() <= 50_000) {
                "Game data pack contains too many items"
            }

            if (activeFile.exists()) {
                val current = runCatching {
                    JSONObject(activeFile.readText(Charsets.UTF_8)).getInt("dataPackVersion")
                }.getOrDefault(-1)
                require(manifest.dataPackVersion >= current) { "Older game data packs cannot replace newer data" }
            }
            if (activeFile.exists()) {
                val backup = File(directory, "previous-pack.json")
                activeFile.copyTo(backup, overwrite = true)
            }
            if (!tempFile.renameTo(activeFile)) {
                tempFile.copyTo(activeFile, overwrite = true)
                tempFile.delete()
            }
            activeFile
        } finally {
            connection.disconnect()
            if (tempFile.exists()) tempFile.delete()
        }
    }

    fun activePack(): File? = activeFile.takeIf { it.isFile && it.length() > 0 }
}

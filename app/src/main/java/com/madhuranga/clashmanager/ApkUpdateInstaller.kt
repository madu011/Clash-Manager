package com.madhuranga.clashmanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest

/**
 * Downloads a trusted release APK, verifies its SHA-256, then opens Android's
 * package installer. Android and package-signature checks remain authoritative.
 */
class ApkUpdateInstaller(
    context: Context,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    }
) {
    private val appContext = context.applicationContext
    private val apkFile = File(appContext.cacheDir, "updates/clash-manager-update.apk")
    private val maxBytes = 100L * 1024L * 1024L

    fun canRequestInstallPackages(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            appContext.packageManager.canRequestPackageInstalls()

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${appContext.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
        }
    }

    private fun openReleaseConnection(startUrl: String): HttpURLConnection {
        var currentUrl = URL(startUrl)
        repeat(4) { redirectCount ->
            val uri = currentUrl.toURI()
            if (redirectCount == 0) {
                require(uri.scheme == "https" && uri.host == "github.com" &&
                    uri.path.startsWith("/madu011/Clash-Manager/releases/download/") &&
                    uri.userInfo == null && !uri.path.contains("..")) {
                    "APK URL is not an approved GitHub release URL"
                }
            } else {
                require(uri.scheme == "https" &&
                    uri.host in setOf("objects.githubusercontent.com", "release-assets.githubusercontent.com") &&
                    uri.userInfo == null) {
                    "APK redirect host is not trusted"
                }
            }
            val connection = connectionFactory(currentUrl)
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/octet-stream")
            connection.connect()
            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_MOVED_PERM ||
                status == HttpURLConnection.HTTP_MOVED_TEMP ||
                status == HttpURLConnection.HTTP_SEE_OTHER ||
                status == 307 || status == 308) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                require(!location.isNullOrBlank() && redirectCount < 3) {
                    "APK download redirect is invalid"
                }
                currentUrl = URL(currentUrl, location)
            } else {
                return connection
            }
        }
        error("Too many APK download redirects")
    }

    fun downloadAndLaunch(manifest: UpdateManifest): Result<File> = runCatching {
        require(canRequestInstallPackages()) { "Allow Clash Manager to install app updates in Android settings first" }
        val uri = URI(manifest.apkUrl)
        require(uri.scheme == "https" && uri.host == "github.com")
        require(uri.path.startsWith("/madu011/Clash-Manager/releases/download/"))
        require(!uri.path.contains("..") && uri.userInfo == null)

        apkFile.parentFile?.mkdirs()
        val tempFile = File(apkFile.parentFile, "clash-manager-update.tmp")
        val connection = openReleaseConnection(manifest.apkUrl)
        try {
            require(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "APK download failed (HTTP ${connection.responseCode})"
            }
            val length = connection.contentLengthLong
            require(length in 1..maxBytes) { "APK size is invalid or exceeds 100 MB" }
            val digest = MessageDigest.getInstance("SHA-256")
            var total = 0L
            connection.inputStream.use { input ->
                tempFile.outputStream().buffered().use { output ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= maxBytes) { "APK exceeds the 100 MB size limit" }
                        digest.update(buffer, 0, read)
                        output.write(buffer, 0, read)
                    }
                }
            }
            require(total > 0) { "Downloaded APK is empty" }
            val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
            require(actualHash.equals(manifest.apkSha256, ignoreCase = true)) {
                "APK checksum does not match"
            }
            require(tempFile.renameTo(apkFile) || run {
                tempFile.copyTo(apkFile, overwrite = true)
                tempFile.delete()
                true
            }) { "Could not save downloaded APK" }
        } finally {
            connection.disconnect()
            if (tempFile.exists()) tempFile.delete()
        }

        val contentUri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            apkFile
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        appContext.startActivity(installIntent)
        apkFile
    }
}

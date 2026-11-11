package com.madhuranga.clashmanager

import org.json.JSONObject

data class UpdateManifest(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val apkUrl: String,
    val apkSha256: String,
    val minimumSupportedVersionCode: Int,
    val dataPackVersion: Int,
    val dataPackUrl: String,
    val dataPackSha256: String
) {
    companion object {
        fun parse(raw: String): UpdateManifest {
            val json = JSONObject(raw)
            val manifest = UpdateManifest(
                latestVersionCode = json.getInt("latestVersionCode"),
                latestVersionName = json.getString("latestVersionName"),
                apkUrl = json.getString("apkUrl"),
                apkSha256 = json.getString("apkSha256").lowercase(),
                minimumSupportedVersionCode = json.getInt("minimumSupportedVersionCode"),
                dataPackVersion = json.getInt("dataPackVersion"),
                dataPackUrl = json.getString("dataPackUrl"),
                dataPackSha256 = json.getString("dataPackSha256").lowercase()
            )
            require(manifest.latestVersionCode > 0) { "Invalid app version code" }
            require(manifest.minimumSupportedVersionCode in 1..manifest.latestVersionCode) {
                "Invalid minimum supported app version"
            }
            require(manifest.latestVersionName.isNotBlank() && manifest.latestVersionName.length <= 40) {
                "Invalid app version name"
            }
            require(manifest.dataPackVersion >= 0) { "Invalid data pack version" }
            require(isTrustedReleaseUrl(manifest.apkUrl)) { "APK must use the official GitHub release host" }
            require(isTrustedDataUrl(manifest.dataPackUrl)) { "Data pack must use the official raw GitHub host" }
            require(SHA256.matches(manifest.apkSha256)) { "Invalid APK SHA-256 checksum" }
            require(SHA256.matches(manifest.dataPackSha256)) { "Invalid data pack SHA-256 checksum" }
            return manifest
        }

        private val SHA256 = Regex("^[a-f0-9]{64}$")

        private fun isTrustedReleaseUrl(value: String): Boolean = runCatching {
            val uri = java.net.URI(value)
            uri.scheme == "https" &&
                uri.host == "github.com" &&
                uri.path.startsWith("/madu011/Clash-Manager/releases/download/") &&
                !uri.path.contains("..") &&
                uri.userInfo == null
        }.getOrDefault(false)

        private fun isTrustedDataUrl(value: String): Boolean = runCatching {
            val uri = java.net.URI(value)
            uri.scheme == "https" &&
                uri.host == "raw.githubusercontent.com" &&
                uri.path.startsWith("/madu011/Clash-Manager/") &&
                !uri.path.contains("..") &&
                uri.userInfo == null
        }.getOrDefault(false)
    }
}

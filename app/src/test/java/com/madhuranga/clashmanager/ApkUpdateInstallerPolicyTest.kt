package com.madhuranga.clashmanager

import org.junit.Assert.assertFalse
import org.junit.Test

class ApkUpdateInstallerPolicyTest {
    @Test fun manifestRejectsNonReleaseApkHost() {
        val raw = """{"latestVersionCode":2,"latestVersionName":"1.1.0","apkUrl":"https://example.com/app.apk","apkSha256":"${"a".repeat(64)}","minimumSupportedVersionCode":1,"dataPackVersion":0,"dataPackUrl":"https://raw.githubusercontent.com/madu011/Clash-Manager/main/data/pack.json","dataPackSha256":"${"b".repeat(64)}"}"""
        val result = runCatching { UpdateManifest.parse(raw) }
        assertFalse(result.isSuccess)
    }
}

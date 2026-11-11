package com.madhuranga.clashmanager

/** Returns true when the installed app is below the server-declared minimum version. */
fun isMandatoryUpdateRequired(installedVersionCode: Int, minimumSupportedVersionCode: Int): Boolean =
    installedVersionCode > 0 &&
        minimumSupportedVersionCode > 0 &&
        installedVersionCode < minimumSupportedVersionCode

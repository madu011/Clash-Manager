package com.madhuranga.clashmanager

/**
 * Local, pre-upload checks for an attached base screenshot.
 * No image bytes are uploaded by this validator.
 */
object AttackScreenshotValidator {
    const val MAX_IMAGE_BYTES: Long = 10L * 1024L * 1024L

    fun validate(mimeType: String?, byteCount: Long?): String? {
        if (mimeType == null || !mimeType.startsWith("image/")) {
            return "Choose a valid image file."
        }
        if (byteCount == null || byteCount <= 0L) {
            return "The selected image could not be read."
        }
        if (byteCount > MAX_IMAGE_BYTES) {
            return "Image must be 10 MB or smaller."
        }
        return null
    }
}

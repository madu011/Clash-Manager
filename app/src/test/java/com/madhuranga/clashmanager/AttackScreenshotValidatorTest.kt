package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AttackScreenshotValidatorTest {
    @Test
    fun acceptsImageWithinLimit() {
        assertNull(AttackScreenshotValidator.validate("image/jpeg", 1024L))
    }

    @Test
    fun rejectsNonImageMimeType() {
        assertEquals("Choose a valid image file.", AttackScreenshotValidator.validate("text/plain", 100L))
    }

    @Test
    fun rejectsMissingOrEmptyImage() {
        assertEquals("Choose a valid image file.", AttackScreenshotValidator.validate(null, 100L))
        assertEquals("The selected image could not be read.", AttackScreenshotValidator.validate("image/png", 0L))
    }

    @Test
    fun rejectsImagesLargerThanTenMegabytes() {
        assertEquals(
            "Image must be 10 MB or smaller.",
            AttackScreenshotValidator.validate("image/png", AttackScreenshotValidator.MAX_IMAGE_BYTES + 1L)
        )
    }
}

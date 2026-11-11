package com.madhuranga.clashmanager

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {
    @Test
    fun olderVersionRequiresUpdate() {
        assertTrue(isMandatoryUpdateRequired(10, 11))
    }

    @Test
    fun minimumVersionIsStillSupported() {
        assertFalse(isMandatoryUpdateRequired(11, 11))
    }

    @Test
    fun newerVersionIsSupported() {
        assertFalse(isMandatoryUpdateRequired(12, 11))
    }

    @Test
    fun invalidVersionValuesDoNotForceLockout() {
        assertFalse(isMandatoryUpdateRequired(0, 11))
        assertFalse(isMandatoryUpdateRequired(10, 0))
    }
}

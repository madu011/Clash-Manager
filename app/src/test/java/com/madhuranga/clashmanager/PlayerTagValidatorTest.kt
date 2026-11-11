package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerTagValidatorTest {
    @Test fun normalizesValidTagAndKeepsPrefix() {
        assertEquals("#UG028J2C", PlayerTagValidator.normalize("#ug028j2c"))
    }

    @Test fun acceptsMaximumLengthTag() {
        assertTrue(PlayerTagValidator.isValid("#0289PYLQGRJCUV"))
    }

    @Test fun rejectsInvalidCharactersAndLengths() {
        assertFalse(PlayerTagValidator.isValid("#ABC123"))
        assertFalse(PlayerTagValidator.isValid("#AB"))
        assertFalse(PlayerTagValidator.isValid("#0289PYLQGRJCUV00"))
    }

    @Test fun removesInvalidCharactersDuringNormalization() {
        assertEquals("#UG028J2C", PlayerTagValidator.normalize("#ug-028 j2c"))
    }
}

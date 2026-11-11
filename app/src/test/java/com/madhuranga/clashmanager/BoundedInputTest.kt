package com.madhuranga.clashmanager

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BoundedInputTest {
    @Test fun readsUtf8WithinLimit() {
        val value = readUtf8Limited(ByteArrayInputStream("hello".toByteArray()), 5)
        assertEquals("hello", value)
    }

    @Test fun rejectsInputLargerThanLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            readUtf8Limited(ByteArrayInputStream("123456".toByteArray()), 5)
        }
    }

    @Test fun rejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            readUtf8Limited(ByteArrayInputStream(byteArrayOf()), 0)
        }
    }
}

package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GameDataPackParserTest {
    private fun pack(items: String = """{"dataId":1000008,"name":"Cannon","base":"home","category":"buildings","maxLevel":18}""") =
        """{"schemaVersion":1,"dataPackVersion":4,"items":[$items]}"""

    @Test fun parsesKnownEntry() {
        val parsed = GameDataPackParser.parse(pack(), 4)
        assertEquals(4, parsed.version)
        assertEquals("Cannon", parsed.items.single().name)
        assertEquals(18, parsed.items.single().maxLevel)
    }

    @Test fun rejectsWrongVersion() {
        assertThrows(IllegalArgumentException::class.java) { GameDataPackParser.parse(pack(), 3) }
    }

    @Test fun rejectsUnknownCategory() {
        val invalid = """{"dataId":1000008,"name":"Cannon","base":"home","category":"unknown"}"""
        assertThrows(IllegalArgumentException::class.java) { GameDataPackParser.parse(pack(invalid), 4) }
    }

    @Test fun rejectsDuplicateEntries() {
        val item = """{"dataId":1000008,"name":"Cannon","base":"home","category":"buildings"}"""
        assertThrows(IllegalArgumentException::class.java) { GameDataPackParser.parse(pack("$item,$item"), 4) }
    }

    @Test fun rejectsBaseCategoryMismatch() {
        val invalid = """{"dataId":1000037,"name":"Gold Mine","base":"home","category":"buildings2"}"""
        assertThrows(IllegalArgumentException::class.java) { GameDataPackParser.parse(pack(invalid), 4) }
    }
}

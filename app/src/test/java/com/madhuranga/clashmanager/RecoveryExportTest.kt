package com.madhuranga.clashmanager

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryExportTest {
    @Test fun exportsCsvWithEscapedBuildingNamesAndLevelGain() {
        val csv = RecoveryExport.toCsv(
            listOf(RecoveryHistoryEntry("Archer, \"Tower\"", 4, 6, 123L))
        )
        assertTrue(csv.startsWith("building,from_level,to_level,levels_gained,recorded_at_millis"))
        assertTrue(csv.contains("\"Archer, \"\"Tower\"\"",4,6,2,123"))
    }

    @Test fun exportsJsonWithPlayerAndHistory() {
        val json = JSONObject(
            RecoveryExport.toJson(
                "#ABC123",
                listOf(RecoveryHistoryEntry("Cannon", 10, 12, 456L)),
                789L
            )
        )
        assertEquals(1, json.getInt("formatVersion"))
        assertEquals("#ABC123", json.getString("playerTag"))
        assertEquals(789L, json.getLong("exportedAtMillis"))
        assertEquals(1, json.getInt("historyCount"))
        assertEquals(2, json.getJSONArray("history").getJSONObject(0).getInt("levelsGained"))
    }

    @Test fun exportsEmptyHistoryAsHeaderOnlyCsvAndEmptyJsonArray() {
        assertEquals(1, RecoveryExport.toCsv(emptyList()).lines().size)
        assertEquals(0, JSONObject(RecoveryExport.toJson("tag", emptyList(), 10L))
            .getJSONArray("history").length())
    }
}

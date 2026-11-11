package com.madhuranga.clashmanager

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RecoveryExport {
    fun toCsv(history: List<RecoveryHistoryEntry>): String {
        val rows = mutableListOf("building,from_level,to_level,levels_gained,recorded_at_millis")
        history.forEach { entry ->
            val safeName = "\"" + entry.buildingName.replace("\"", "\"\"") + "\""
            rows += listOf(
                safeName,
                entry.fromLevel.toString(),
                entry.toLevel.toString(),
                (entry.toLevel - entry.fromLevel).toString(),
                entry.recordedAtMillis.toString()
            ).joinToString(",")
        }
        return rows.joinToString("\n")
    }

    fun toJson(
        playerTag: String,
        history: List<RecoveryHistoryEntry>,
        exportedAtMillis: Long
    ): String = JSONObject().apply {
        put("formatVersion", 1)
        put("playerTag", playerTag)
        put("exportedAtMillis", exportedAtMillis)
        put("historyCount", history.size)
        put("history", JSONArray().apply {
            history.forEach { entry ->
                put(JSONObject().apply {
                    put("buildingName", entry.buildingName)
                    put("fromLevel", entry.fromLevel)
                    put("toLevel", entry.toLevel)
                    put("levelsGained", entry.toLevel - entry.fromLevel)
                    put("recordedAtMillis", entry.recordedAtMillis)
                })
            }
        })
    }.toString(2)
}

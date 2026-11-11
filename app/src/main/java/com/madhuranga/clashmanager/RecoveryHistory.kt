package com.madhuranga.clashmanager

import org.json.JSONArray
import org.json.JSONObject

data class RecoveryHistoryEntry(
    val buildingName: String,
    val fromLevel: Int,
    val toLevel: Int,
    val recordedAtMillis: Long
)

object RecoveryHistory {
    fun recordChanges(
        previous: List<RecoveryHistoryEntry>,
        startingLevels: Map<String, Int>,
        currentLevels: Map<String, Int>,
        recordedAtMillis: Long
    ): List<RecoveryHistoryEntry> {
        val latest = mutableMapOf<String, Int>()
        previous.forEach { entry -> latest[entry.buildingName.lowercase()] = entry.toLevel }
        val additions = currentLevels.mapNotNull { (name, current) ->
            val baseline = latest[name.lowercase()] ?: startingLevels.entries
                .firstOrNull { it.key.equals(name, true) }?.value ?: current
            if (name.isBlank() || baseline < 0 || current !in 0..100 || current <= baseline) null
            else RecoveryHistoryEntry(name, baseline, current, recordedAtMillis)
        }
        return (previous + additions).takeLast(200)
    }

    fun encode(entries: List<RecoveryHistoryEntry>): String = JSONArray().apply {
        entries.forEach { entry ->
            put(JSONObject().apply {
                put("buildingName", entry.buildingName)
                put("fromLevel", entry.fromLevel)
                put("toLevel", entry.toLevel)
                put("recordedAtMillis", entry.recordedAtMillis)
            })
        }
    }.toString()

    fun decode(raw: String): List<RecoveryHistoryEntry> = runCatching {
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val name = item.optString("buildingName").trim()
            val from = item.optInt("fromLevel", -1)
            val to = item.optInt("toLevel", -1)
            val at = item.optLong("recordedAtMillis", -1L)
            if (name.isBlank() || from !in 0..100 || to !in 1..100 || to <= from || at < 0) null
            else RecoveryHistoryEntry(name, from, to, at)
        }.takeLast(200)
    }.getOrDefault(emptyList())
}

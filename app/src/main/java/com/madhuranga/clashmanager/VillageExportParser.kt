package com.madhuranga.clashmanager

import org.json.JSONArray
import org.json.JSONObject

data class ExportItem(val dataId: Long, val level: Int?, val count: Int)
data class ExportCategory(val key: String, val itemCount: Int, val totalCount: Int, val items: List<ExportItem>)
data class VillageExportSnapshot(val playerTag: String?, val timestamp: Long?, val categories: List<ExportCategory>) {
    fun summary(): String = categories.joinToString(" • ") { "${it.key}: ${it.totalCount}" }
}

/** Reads the observed export collections without guessing names from numeric game IDs. */
object VillageExportParser {
    private val categoryKeys = listOf(
        "buildings", "traps", "decos", "obstacles", "units", "siege_machines",
        "heroes", "spells", "pets", "equipment", "buildings2", "traps2",
        "decos2", "obstacles2", "units2", "heroes2"
    )

    fun parse(raw: String): VillageExportSnapshot {
        val root = JSONObject(raw)
        val categories = categoryKeys.mapNotNull { key ->
            val array = root.optJSONArray(key) ?: return@mapNotNull null
            parseCategory(key, array)
        }
        require(categories.isNotEmpty()) { "No recognised village-export collections were found" }
        return VillageExportSnapshot(
            playerTag = root.optString("tag").takeIf { it.isNotBlank() && it != "null" },
            timestamp = root.optLong("timestamp").takeIf { root.has("timestamp") && !root.isNull("timestamp") },
            categories = categories
        )
    }

    private fun parseCategory(key: String, array: JSONArray): ExportCategory {
        data class ItemKey(val id: Long, val level: Int?)
        val grouped = linkedMapOf<ItemKey, Int>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            if (!item.has("data") || item.isNull("data")) continue
            val id = item.optLong("data", -1L)
            if (id < 0L) continue
            val level = if (item.has("lvl") && !item.isNull("lvl")) item.optInt("lvl") else null
            val count = item.optInt("cnt", 1).coerceAtLeast(1)
            val itemKey = ItemKey(id, level)
            grouped[itemKey] = (grouped[itemKey] ?: 0) + count
        }
        val items = grouped.map { (itemKey, count) -> ExportItem(itemKey.id, itemKey.level, count) }
        return ExportCategory(key, array.length(), items.sumOf { it.count }, items)
    }
}

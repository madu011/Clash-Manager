package com.madhuranga.clashmanager

import org.json.JSONObject

data class GameDataPackItem(
    val dataId: Long,
    val name: String,
    val base: String,
    val category: String,
    val maxLevel: Int?
)

data class ParsedGameDataPack(
    val version: Int,
    val items: List<GameDataPackItem>
)

/** Strict parser for the versioned, remotely supplied game-data catalogue. */
object GameDataPackParser {
    private val allowedBases = setOf("home", "builder")
    private val allowedCategories = setOf(
        "buildings", "traps", "decos", "obstacles", "units", "siege_machines",
        "heroes", "spells", "pets", "equipment", "buildings2", "traps2",
        "decos2", "obstacles2", "units2", "heroes2"
    )

    fun parse(raw: String, expectedVersion: Int): ParsedGameDataPack {
        require(raw.toByteArray(Charsets.UTF_8).size <= 5 * 1024 * 1024) { "Game data pack is too large" }
        val root = JSONObject(raw)
        require(root.getInt("schemaVersion") == 1) { "Unsupported game data schema" }
        val version = root.getInt("dataPackVersion")
        require(version == expectedVersion && version >= 0) { "Game data version mismatch" }
        val array = root.getJSONArray("items")
        require(array.length() in 1..50_000) { "Game data item count is invalid" }
        val seen = HashSet<String>()
        val items = (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val id = item.getLong("dataId")
            val name = item.getString("name").trim()
            val base = item.getString("base")
            val category = item.getString("category")
            require(id > 0) { "Invalid game data ID" }
            require(name.isNotBlank() && name.length <= 80) { "Invalid game data name" }
            require(base in allowedBases) { "Unknown game data base" }
            require(category in allowedCategories) { "Unknown game data category" }
            require((base == "home") == !category.endsWith("2")) {
                "Game data category does not match its village base"
            }
            val maxLevel = if (item.has("maxLevel") && !item.isNull("maxLevel")) item.getInt("maxLevel") else null
            require(maxLevel == null || maxLevel in 1..100) { "Invalid maximum level" }
            val key = "$base:$category:$id"
            require(seen.add(key)) { "Duplicate game data entry" }
            GameDataPackItem(id, name, base, category, maxLevel)
        }
        return ParsedGameDataPack(version, items)
    }
}

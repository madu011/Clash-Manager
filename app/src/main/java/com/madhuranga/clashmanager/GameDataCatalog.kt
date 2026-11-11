package com.madhuranga.clashmanager

/**
 * Verified ID-to-name seed catalog.
 * Source: chiefpansancolt/clash-of-clans-data (MIT), dataId/name/base fields.
 * Collection-aware lookup prevents Home Village and Builder Base IDs being mixed.
 * Unknown or unsupported collection keys intentionally return null.
 */
object GameDataCatalog {
    private data class Entry(val dataId: Long, val name: String, val base: String)

    private val entries = listOf(
        Entry(1000002L, "Elixir Collector", "home"),
        Entry(1000004L, "Gold Mine", "home"),
        Entry(1000005L, "Gold Storage", "home"),
        Entry(1000008L, "Cannon", "home"),
        Entry(1000009L, "Archer Tower", "home"),
        Entry(1000013L, "Mortar", "home"),
        Entry(26000000L, "Lightning Spell", "home"),
        Entry(28000000L, "Barbarian King", "home"),
        Entry(28000001L, "Archer Queen", "home"),
        Entry(4000000L, "Barbarian", "home"),
        Entry(4000001L, "Archer", "home"),
        Entry(4000002L, "Goblin", "home"),
        Entry(4000003L, "Giant", "home"),
        Entry(4000008L, "Dragon", "home"),
        Entry(1000037L, "Gold Mine", "builder"),
        Entry(1000044L, "Cannon", "builder"),
        Entry(1000048L, "Archer Tower", "builder"),
        Entry(28000003L, "Battle Machine", "builder"),
        Entry(28000005L, "Battle Copter", "builder"),
        Entry(4000032L, "Sneaky Archer", "builder")
    )

    private val homeCollections = setOf(
        "buildings", "traps", "decos", "obstacles", "units",
        "siege_machines", "heroes", "spells", "pets", "equipment"
    )
    private val builderCollections = setOf(
        "buildings2", "traps2", "decos2", "obstacles2", "units2", "heroes2"
    )

    fun nameFor(dataId: Long, collectionKey: String): String? {
        val base = when (collectionKey) {
            in homeCollections -> "home"
            in builderCollections -> "builder"
            else -> return null
        }
        return entries.firstOrNull { it.dataId == dataId && it.base == base }?.name
    }

    /** Compatibility lookup only resolves IDs that have one unambiguous name. */
    fun nameFor(dataId: Long): String? =
        entries.filter { it.dataId == dataId }.map { it.name }.distinct().singleOrNull()
}

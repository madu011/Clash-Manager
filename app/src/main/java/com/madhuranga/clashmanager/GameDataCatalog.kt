package com.madhuranga.clashmanager

/**
 * Verified built-in ID catalogue with an optional validated remote pack overlay.
 * Unknown IDs intentionally remain unresolved.
 */
object GameDataCatalog {
    private data class Entry(val dataId: Long, val name: String, val base: String, val category: String)

    private val entries = listOf(
        Entry(1000002L, "Elixir Collector", "home", "buildings"),
        Entry(1000004L, "Gold Mine", "home", "buildings"),
        Entry(1000005L, "Gold Storage", "home", "buildings"),
        Entry(1000008L, "Cannon", "home", "buildings"),
        Entry(1000009L, "Archer Tower", "home", "buildings"),
        Entry(1000013L, "Mortar", "home", "buildings"),
        Entry(26000000L, "Lightning Spell", "home", "spells"),
        Entry(28000000L, "Barbarian King", "home", "heroes"),
        Entry(28000001L, "Archer Queen", "home", "heroes"),
        Entry(4000000L, "Barbarian", "home", "units"),
        Entry(4000001L, "Archer", "home", "units"),
        Entry(4000002L, "Goblin", "home", "units"),
        Entry(4000003L, "Giant", "home", "units"),
        Entry(4000008L, "Dragon", "home", "units"),
        Entry(1000037L, "Gold Mine", "builder", "buildings2"),
        Entry(1000044L, "Cannon", "builder", "buildings2"),
        Entry(1000048L, "Archer Tower", "builder", "buildings2"),
        Entry(28000003L, "Battle Machine", "builder", "heroes2"),
        Entry(28000005L, "Battle Copter", "builder", "heroes2"),
        Entry(4000032L, "Sneaky Archer", "builder", "units2")
    )

    private val homeCollections = setOf(
        "buildings", "traps", "decos", "obstacles", "units",
        "siege_machines", "heroes", "spells", "pets", "equipment"
    )
    private val builderCollections = setOf(
        "buildings2", "traps2", "decos2", "obstacles2", "units2", "heroes2"
    )

    fun nameFor(dataId: Long, collectionKey: String, pack: ParsedGameDataPack? = null): String? {
        val base = when (collectionKey) {
            in homeCollections -> "home"
            in builderCollections -> "builder"
            else -> return null
        }
        val remote = pack?.items?.firstOrNull {
            it.dataId == dataId && it.base == base && it.category == collectionKey
        }?.name
        if (remote != null) return remote
        return entries.firstOrNull {
            it.dataId == dataId && it.base == base && it.category == collectionKey
        }?.name
    }

    fun nameFor(dataId: Long): String? =
        entries.filter { it.dataId == dataId }.map { it.name }.distinct().singleOrNull()
}

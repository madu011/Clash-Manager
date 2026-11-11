package com.madhuranga.clashmanager

/**
 * Compares verified upgrade costs with balances manually entered by the player.
 * Missing balances are not inferred; callers must provide a value for each resource.
 */
object ResourceBudgetPlanner {
    data class ResourceStatus(
        val resource: String,
        val required: Long,
        val available: Long,
        val shortage: Long,
        val sufficient: Boolean
    )

    fun compare(
        requiredCosts: Map<String, Long>,
        availableBalances: Map<String, Long>
    ): List<ResourceStatus>? {
        if (requiredCosts.values.any { it < 0L } || availableBalances.values.any { it < 0L }) return null
        if (requiredCosts.keys.any { it.isBlank() } || availableBalances.keys.any { it.isBlank() }) return null

        return requiredCosts.toSortedMap().map { (resource, required) ->
            val available = availableBalances[resource] ?: return null
            val shortage = (required - available).coerceAtLeast(0L)
            ResourceStatus(
                resource = resource,
                required = required,
                available = available,
                shortage = shortage,
                sufficient = shortage == 0L
            )
        }
    }
}

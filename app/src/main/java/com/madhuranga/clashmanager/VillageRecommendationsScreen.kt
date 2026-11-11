package com.madhuranga.clashmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

private val AnalyzerGold = Color(0xFFFFC857)
private val AnalyzerMuted = Color(0xFF9AA8BA)
private val AnalyzerPanel = Color(0xFF1A2230)

private data class BuildingTargetInput(val name: String, val current: Int, val target: Int)

private data class UpgradeEstimate(val name: String, val cost: Long?, val durationSeconds: Long?, val missingLevels: List<Int>)

private fun estimateUpgrades(targets: List<BuildingTargetInput>): List<UpgradeEstimate> =
    targets.map { target ->
        val missing = (target.current + 1..target.target).filter { level ->
            BuildingUpgradeCatalog.nextUpgrade(target.name, level - 1) == null
        }
        val steps = (target.current + 1..target.target).mapNotNull { level ->
            BuildingUpgradeCatalog.nextUpgrade(target.name, level - 1)
        }
        val complete = missing.isEmpty()
        UpgradeEstimate(
            name = target.name,
            cost = if (complete) steps.sumOf { it.cost } else null,
            durationSeconds = if (complete) steps.sumOf { it.durationSeconds } else null,
            missingLevels = missing
        )
    }

private fun formatDuration(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    return listOfNotNull(
        if (days > 0) "${days}d" else null,
        if (hours > 0) "${hours}h" else null,
        if (minutes > 0) "${minutes}m" else null
    ).ifEmpty { listOf("<1m") }.joinToString(" ")
}

private fun parseBuildingTargets(input: String): List<BuildingTargetInput>? {
    if (input.isBlank()) return emptyList()
    val entries = input.split(",")
    if (entries.size > 12) return null
    val parsed = mutableListOf<BuildingTargetInput>()
    for (entry in entries) {
        val fields = entry.trim().split(":")
        if (fields.size != 3) return null
        val name = fields[0].trim()
        val current = fields[1].trim().toIntOrNull() ?: return null
        val target = fields[2].trim().toIntOrNull() ?: return null
        if (name.isBlank() || name.length > 40 || current !in 0..100 || target !in 1..100 || target <= current) return null
        parsed += BuildingTargetInput(name, current, target)
    }
    if (parsed.map { it.name.lowercase() }.distinct().size != parsed.size) return null
    return parsed
}

@Composable
fun VillageRecommendationsScreen(
    playerTag: String,
    townHallLevel: String,
    onTownHallLevelChange: (String) -> Unit,
    availableBuilders: String,
    onAvailableBuildersChange: (String) -> Unit,
    rushedBuildings: String,
    onRushedBuildingsChange: (String) -> Unit,
    buildingTargets: String,
    onBuildingTargetsChange: (String) -> Unit
) {
    var recommendations by remember(playerTag) {
        mutableStateOf(emptyList<UpgradeRecommendation>())
    }
    var validationMessage by remember(playerTag) { mutableStateOf("") }
    var upgradeEstimates by remember(playerTag) { mutableStateOf(emptyList<UpgradeEstimate>()) }
    var builderSchedule by remember(playerTag) { mutableStateOf<BuilderSchedulePlanner.Result?>(null) }
    var goldBalance by remember(playerTag) { mutableStateOf("0") }
    var elixirBalance by remember(playerTag) { mutableStateOf("0") }
    var darkElixirBalance by remember(playerTag) { mutableStateOf("0") }
    var resourceBudget by remember(playerTag) { mutableStateOf<List<ResourceBudgetPlanner.ResourceStatus>?>(null) }
    var recoveryTasks by remember(playerTag) { mutableStateOf<List<RushRecoveryPlanner.Task>>(emptyList()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = AnalyzerPanel),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "VILLAGE UPGRADE ADVISOR",
                color = AnalyzerGold,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Enter only details you know. Recommendations are based on your entries, not hidden game data.",
                color = AnalyzerMuted,
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = townHallLevel,
                onValueChange = { onTownHallLevelChange(it.filter(Char::isDigit).take(2)) },
                label = { Text("Town Hall level (1–18)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = availableBuilders,
                onValueChange = { onAvailableBuildersChange(it.filter(Char::isDigit).take(1)) },
                label = { Text("Available builders (0–6)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = rushedBuildings,
                onValueChange = { onRushedBuildingsChange(it.take(300)) },
                label = { Text("Buildings behind (comma separated)") },
                supportingText = { Text("Example: Archer Tower, Cannon — only enter buildings you have checked.") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = buildingTargets,
                onValueChange = { onBuildingTargetsChange(it.take(500)) },
                label = { Text("Building level targets") },
                supportingText = { Text("Format: Archer Tower:10:15, Cannon:12:15 (name:current:target)") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("CURRENT RESOURCE BALANCES", color = AnalyzerGold, fontWeight = FontWeight.Bold)
            Text("Enter your current in-game balances. The planner compares them with verified upgrade costs.", color = AnalyzerMuted, style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = goldBalance,
                onValueChange = { goldBalance = it.filter(Char::isDigit).take(12) },
                label = { Text("Available Gold") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = elixirBalance,
                onValueChange = { elixirBalance = it.filter(Char::isDigit).take(12) },
                label = { Text("Available Elixir") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = darkElixirBalance,
                onValueChange = { darkElixirBalance = it.filter(Char::isDigit).take(12) },
                label = { Text("Available Dark Elixir") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val th = townHallLevel.toIntOrNull()
                    val builders = availableBuilders.toIntOrNull()
                    val parsedTargets = parseBuildingTargets(buildingTargets)
                    when {
                        playerTag.isBlank() -> {
                            validationMessage = "Save a Player Tag before creating profile recommendations."
                            recommendations = emptyList()
                            upgradeEstimates = emptyList()
                            builderSchedule = null
                            resourceBudget = null
                            recoveryTasks = emptyList()
                        }
                        th == null || th !in 1..18 -> {
                            validationMessage = "Enter a Town Hall level from 1 to 18."
                            recommendations = emptyList()
                            upgradeEstimates = emptyList()
                            builderSchedule = null
                            resourceBudget = null
                            recoveryTasks = emptyList()
                        }
                        builders == null || builders !in 0..6 -> {
                            validationMessage = "Enter available builders from 0 to 6."
                            recommendations = emptyList()
                            upgradeEstimates = emptyList()
                            builderSchedule = null
                            resourceBudget = null
                            recoveryTasks = emptyList()
                        }
                        parsedTargets == null -> {
                            validationMessage = "Check building entries. Use name:current:target, with target above current."
                            recommendations = emptyList()
                            upgradeEstimates = emptyList()
                            builderSchedule = null
                            resourceBudget = null
                            recoveryTasks = emptyList()
                        }
                        parsedTargets.any { target ->
                            val verifiedMaximum = BuildingUpgradeCatalog.maximumLevelAt(target.name, th)
                            verifiedMaximum != null && target.target > verifiedMaximum
                        } -> {
                            val invalidTarget = parsedTargets.first { target ->
                                val verifiedMaximum = BuildingUpgradeCatalog.maximumLevelAt(target.name, th)
                                verifiedMaximum != null && target.target > verifiedMaximum
                            }
                            val maximum = BuildingUpgradeCatalog.maximumLevelAt(invalidTarget.name, th)
                            validationMessage = "${invalidTarget.name}: Town Hall $th currently allows up to level $maximum in the verified catalog."
                            recommendations = emptyList()
                            upgradeEstimates = emptyList()
                            builderSchedule = null
                            resourceBudget = null
                            recoveryTasks = emptyList()
                        }
                        else -> {
                            upgradeEstimates = estimateUpgrades(parsedTargets)
                            builderSchedule = BuilderSchedulePlanner.create(
                                parsedTargets.map { BuilderSchedulePlanner.Target(it.name, it.current, it.target) },
                                builders
                            )
                            resourceBudget = builderSchedule?.let { schedule ->
                                ResourceBudgetPlanner.compare(
                                    requiredCosts = schedule.totalCosts,
                                    availableBalances = mapOf(
                                        "Gold" to (goldBalance.toLongOrNull() ?: 0L),
                                        "Elixir" to (elixirBalance.toLongOrNull() ?: 0L),
                                        "Dark Elixir" to (darkElixirBalance.toLongOrNull() ?: 0L)
                                    )
                                )
                            }
                            val snapshot = VillageSnapshot(
                                    playerTag = playerTag,
                                    townHallLevel = th,
                                    availableBuilders = builders,
                                    buildingLevels = parsedTargets.associate { it.name to it.current },
                                    buildingTargetLevels = parsedTargets.associate { it.name to it.target },
                                    rushedBuildings = rushedBuildings.split(",")
                                        .map(String::trim)
                                        .filter(String::isNotBlank)
                                        .distinct()
                                        .take(8)
                                )
                            recommendations = VillageAnalyzer.analyze(snapshot)
                            recoveryTasks = RushRecoveryPlanner.create(snapshot)
                            validationMessage = "Based on the profile details you entered. Verify each suggestion in-game."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GENERATE UPGRADE PRIORITIES")
            }
            if (validationMessage.isNotBlank()) {
                Text(validationMessage, color = AnalyzerMuted, style = MaterialTheme.typography.bodySmall)
            }
            if (recoveryTasks.isNotEmpty()) {
                Text("RUSH RECOVERY CHECKLIST", color = AnalyzerGold, fontWeight = FontWeight.Bold)
                Text("Tasks are ranked from the level gaps you entered. Unreported buildings are not assumed to be rushed.", color = AnalyzerMuted, style = MaterialTheme.typography.bodySmall)
                recoveryTasks.forEachIndexed { index, task ->
                    Surface(
                        color = Color(0xFF253244),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${index + 1}. ${task.buildingName}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Level ${task.currentLevel} → ${task.targetLevel} • ${task.remainingLevels} level(s) remaining", color = AnalyzerMuted)
                            Text("Recovery priority: ${task.priority}", color = AnalyzerGold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            if (upgradeEstimates.isNotEmpty()) {
                Text("VERIFIED UPGRADE ESTIMATES", color = AnalyzerGold, fontWeight = FontWeight.Bold)
                Text("Only complete, verified catalog ranges are totaled. Time is cumulative builder time.", color = AnalyzerMuted, style = MaterialTheme.typography.bodySmall)
                if (upgradeEstimates.all { it.cost != null && it.durationSeconds != null }) {
                    val totalCost = upgradeEstimates.sumOf { it.cost ?: 0L }
                    val totalBuilderSeconds = upgradeEstimates.sumOf { it.durationSeconds ?: 0L }
                    Surface(
                        color = Color(0xFF303D50),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("TOTAL VERIFIED PLAN", color = Color.White, fontWeight = FontWeight.ExtraBold)
                            Text("Total Gold: ${String.format(Locale.US, "%,d", totalCost)}", color = AnalyzerGold)
                            Text("Combined builder time: ${formatDuration(totalBuilderSeconds)}", color = AnalyzerMuted)
                            Text("Assumes one builder works continuously; this is not a calendar finish date.", color = AnalyzerMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Text(
                        "Overall total unavailable because one or more building ranges are missing verified data.",
                        color = AnalyzerMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (builderSchedule != null) {
                    Text("BUILDER SCHEDULE", color = AnalyzerGold, fontWeight = FontWeight.Bold)
                    Text(
                        "Estimated completion with $availableBuilders available builder(s): ${formatDuration(builderSchedule!!.elapsedSeconds)}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Illustrative schedule based on verified durations. It assumes builders are free now and does not account for resources, boosts, or other game restrictions.",
                        color = AnalyzerMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                    builderSchedule!!.upgrades.forEach { item ->
                        Text(
                            "Builder ${item.builderNumber} • ${item.buildingName} → Lv ${item.targetLevel} • ${formatDuration(item.startAfterSeconds)}–${formatDuration(item.finishAfterSeconds)}",
                            color = AnalyzerMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Text(
                        "Builder schedule unavailable: enter at least one available builder and use only upgrade ranges with complete verified data.",
                        color = AnalyzerMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (resourceBudget != null) {
                    Text("RESOURCE BUDGET CHECK", color = AnalyzerGold, fontWeight = FontWeight.Bold)
                    resourceBudget!!.forEach { status ->
                        Surface(
                            color = Color(0xFF253244),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(status.resource, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Required: ${String.format(Locale.US, "%,d", status.required)}", color = AnalyzerMuted)
                                Text("Available: ${String.format(Locale.US, "%,d", status.available)}", color = AnalyzerMuted)
                                Text(
                                    if (status.sufficient) "READY — enough resources"
                                    else "SHORTAGE: ${String.format(Locale.US, "%,d", status.shortage)}",
                                    color = if (status.sufficient) Color(0xFF75D69C) else AnalyzerGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                upgradeEstimates.forEach { estimate ->
                    Surface(color = Color(0xFF253244), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(estimate.name, color = Color.White, fontWeight = FontWeight.Bold)
                            if (estimate.cost != null && estimate.durationSeconds != null) {
                                Text("Cost: ${String.format(Locale.US, "%,d", estimate.cost)} Gold", color = AnalyzerGold)
                                Text("Builder time: ${formatDuration(estimate.durationSeconds)}", color = AnalyzerMuted)
                            } else {
                                Text("Estimate unavailable for level(s): ${estimate.missingLevels.joinToString()}. Catalog data is incomplete.", color = AnalyzerMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            recommendations.forEach { recommendation ->
                Surface(
                    color = Color(0xFF253244),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row {
                            Text(
                                recommendation.category.uppercase(),
                                color = AnalyzerGold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text("Priority ${recommendation.priority}", color = AnalyzerMuted, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(recommendation.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(recommendation.reason, color = AnalyzerMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

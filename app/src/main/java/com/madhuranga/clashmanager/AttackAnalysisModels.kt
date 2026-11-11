package com.madhuranga.clashmanager

import android.net.Uri

/** Typed contract for a future real AI provider. No mock strategy or fabricated probability is included. */
enum class AttackMode { CLAN_WAR, CLAN_WAR_LEAGUE, MULTIPLAYER, FRIENDLY }

data class AttackAnalysisRequest(
    val playerTag: String,
    val mode: AttackMode,
    val ownTownHallLevel: Int,
    val targetTownHallLevel: Int,
    val starGoal: Int,
    val baseScreenshot: Uri,
    val playerProfile: AttackingProfile,
    val notes: String = ""
)

data class AttackingProfile(
    val troopLevels: Map<String, Int> = emptyMap(),
    val spellLevels: Map<String, Int> = emptyMap(),
    val heroLevels: Map<String, Int> = emptyMap(),
    val availableSiegeMachines: List<String> = emptyList(),
    val sourceDescription: String = "Not supplied"
)

data class DetectedBaseFeature(
    val feature: String,
    val confidence: Float?,
    val locationDescription: String,
    val verifiedByUser: Boolean = false
)

data class RecommendedUnit(
    val name: String,
    val quantity: Int?,
    val level: Int?,
    val reason: String
)

data class StarOutcomeEstimate(
    val stars: Int,
    val probability: Float?,
    val confidence: Float?,
    val explanation: String
)

data class AttackAnalysisReport(
    val detectedFeatures: List<DetectedBaseFeature>,
    val army: List<RecommendedUnit>,
    val spells: List<RecommendedUnit>,
    val heroes: List<RecommendedUnit>,
    val siegeMachine: String?,
    val attackSequence: List<String>,
    val starEstimates: List<StarOutcomeEstimate>,
    val limitations: List<String>,
    val modelVersion: String
)

sealed interface AttackAnalysisResult {
    data class Success(val report: AttackAnalysisReport) : AttackAnalysisResult
    data class NeedsMoreInformation(val missingItems: List<String>) : AttackAnalysisResult
    data class Failure(val userMessage: String) : AttackAnalysisResult
}

/** Implementations may run on-device or call an authenticated backend. */
interface AttackAnalysisProvider {
    suspend fun analyze(request: AttackAnalysisRequest): AttackAnalysisResult
}

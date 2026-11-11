package com.madhuranga.clashmanager

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class VillagePlannerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { updateOne(context, manager, it) }
    }

    companion object {
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, VillagePlannerWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val prefs = context.getSharedPreferences("clash_manager", Context.MODE_PRIVATE)
            val tag = prefs.getString("player_tag", "").orEmpty()
            val profileKey = tag.ifBlank { "unselected" }
            val townHall = prefs.getString("town_hall_level_$profileKey", "").orEmpty()
            val buildersText = prefs.getString("available_builders_$profileKey", "0").orEmpty()
            val targetsText = prefs.getString("building_targets_$profileKey", "").orEmpty()
            val views = RemoteViews(context.packageName, R.layout.village_planner_widget)

            views.setTextViewText(
                R.id.widget_profile,
                if (tag.isBlank()) "No player profile selected" else "Player $tag • TH ${townHall.ifBlank { "?" }}"
            )

            val builders = buildersText.toIntOrNull()
            val targets = parseTargets(targetsText)
            val result = if (builders != null && targets != null) {
                BuilderSchedulePlanner.create(targets, builders)
            } else null

            val summary = when {
                targetsText.isBlank() -> "Add building targets in Village Planner."
                builders == null || builders !in 1..6 -> "Set 1–6 available builders in the app."
                targets == null -> "Check target format in the app."
                result == null -> "Schedule unavailable: verified upgrade data is incomplete."
                else -> "${targets.size} building target(s) • $builders builder(s)"
            }
            views.setTextViewText(R.id.widget_summary, summary)
            views.setTextViewText(
                R.id.widget_schedule,
                result?.let { "Estimated time: ${formatDuration(it.elapsedSeconds)}" }
                    ?: "Tap to open Clash Manager"
            )

            val openIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_profile, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_summary, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_schedule, pendingIntent)
            manager.updateAppWidget(widgetId, views)
        }

        private fun parseTargets(input: String): List<BuilderSchedulePlanner.Target>? {
            if (input.isBlank()) return emptyList()
            val parsed = input.split(",").map { entry ->
                val fields = entry.trim().split(":")
                if (fields.size != 3) return null
                val name = fields[0].trim()
                val current = fields[1].trim().toIntOrNull() ?: return null
                val target = fields[2].trim().toIntOrNull() ?: return null
                if (name.isBlank() || current < 0 || target <= current) return null
                BuilderSchedulePlanner.Target(name, current, target)
            }
            if (parsed.size > 12 || parsed.map { it.buildingName.lowercase() }.distinct().size != parsed.size) return null
            return parsed
        }

        private fun formatDuration(seconds: Long): String {
            val days = seconds / 86_400
            val hours = (seconds % 86_400) / 3_600
            val minutes = (seconds % 3_600) / 60
            return listOfNotNull(
                if (days > 0) "${days}d" else null,
                if (hours > 0) "${hours}h" else null,
                if (minutes > 0) "${minutes}m" else null
            ).ifEmpty { listOf("<1m") }.joinToString(" ")
        }
    }
}

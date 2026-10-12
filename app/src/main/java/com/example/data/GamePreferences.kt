package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.PhoneNumber
import com.example.model.Quest
import com.example.model.RarityTier
import org.json.JSONArray

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sim_game_state", Context.MODE_PRIVATE)

    fun saveBalance(balance: Long) {
        prefs.edit().putLong("key_balance", balance).apply()
    }

    fun loadBalance(defaultVal: Long = 2_500L): Long {
        return prefs.getLong("key_balance", defaultVal)
    }

    fun savePlayerXp(xp: Long) {
        prefs.edit().putLong("key_player_xp", xp).apply()
    }

    fun loadPlayerXp(): Long {
        return prefs.getLong("key_player_xp", 0L)
    }

    fun saveStats(rolls: Long, earned: Long, bestNumber: PhoneNumber?, rarityCounts: Map<RarityTier, Int>) {
        val editor = prefs.edit()
            .putLong("key_total_rolls", rolls)
            .putLong("key_total_earned", earned)

        if (bestNumber != null) {
            editor.putString("key_best_number_json", bestNumber.toJson())
        } else {
            editor.remove("key_best_number_json")
        }

        val countsSerialized = rarityCounts.entries.joinToString(";") { "${it.key.name}:${it.value}" }
        editor.putString("key_rarity_counts", countsSerialized)

        editor.apply()
    }

    data class SavedStatsData(
        val totalRolls: Long,
        val totalEarned: Long,
        val bestNumber: PhoneNumber?,
        val rarityCounts: Map<RarityTier, Int>
    )

    fun loadStats(): SavedStatsData {
        val rolls = prefs.getLong("key_total_rolls", 0L)
        val earned = prefs.getLong("key_total_earned", 0L)
        val bestJson = prefs.getString("key_best_number_json", null)
        val bestNumber = if (bestJson != null) PhoneNumber.fromJson(bestJson) else null

        val countsSerialized = prefs.getString("key_rarity_counts", null)
        val counts = mutableMapOf<RarityTier, Int>()
        if (!countsSerialized.isNullOrBlank()) {
            try {
                countsSerialized.split(";").forEach { item ->
                    val pair = item.split(":")
                    if (pair.size == 2) {
                        try {
                            val tier = RarityTier.valueOf(pair[0])
                            val count = pair[1].toIntOrNull() ?: 0
                            counts[tier] = count
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }

        return SavedStatsData(rolls, earned, bestNumber, counts)
    }

    fun saveLastResetDates(dailyDate: String, weeklyDate: String, monthlyDate: String) {
        prefs.edit()
            .putString("key_last_daily_reset", dailyDate)
            .putString("key_last_weekly_reset", weeklyDate)
            .putString("key_last_monthly_reset", monthlyDate)
            .apply()
    }

    data class QuestResetDates(
        val lastDaily: String,
        val lastWeekly: String,
        val lastMonthly: String
    )

    fun loadLastResetDates(): QuestResetDates {
        return QuestResetDates(
            lastDaily = prefs.getString("key_last_daily_reset", "") ?: "",
            lastWeekly = prefs.getString("key_last_weekly_reset", "") ?: "",
            lastMonthly = prefs.getString("key_last_monthly_reset", "") ?: ""
        )
    }

    fun saveActiveSim(sim: PhoneNumber?) {
        if (sim == null) {
            prefs.edit().remove("key_active_sim_json").remove("key_active_sim").apply()
            return
        }
        prefs.edit().putString("key_active_sim_json", sim.toJson()).apply()
    }

    fun loadActiveSim(): PhoneNumber? {
        val json = prefs.getString("key_active_sim_json", null)
        if (json != null) {
            val parsed = PhoneNumber.fromJson(json)
            if (parsed != null) return parsed
        }
        return null
    }

    fun saveCurrentNumber(sim: PhoneNumber?) {
        if (sim == null) {
            prefs.edit().remove("key_current_sim_json").apply()
            return
        }
        prefs.edit().putString("key_current_sim_json", sim.toJson()).apply()
    }

    fun loadCurrentNumber(): PhoneNumber? {
        val json = prefs.getString("key_current_sim_json", null) ?: return null
        return PhoneNumber.fromJson(json)
    }

    fun saveInventoryBackup(sims: List<PhoneNumber>) {
        try {
            val arr = JSONArray()
            for (sim in sims) {
                arr.put(sim.toJson())
            }
            prefs.edit().putString("key_inventory_backup_json", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun loadInventoryBackup(): List<PhoneNumber> {
        val raw = prefs.getString("key_inventory_backup_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<PhoneNumber>()
            for (i in 0 until arr.length()) {
                val itemJson = arr.getString(i)
                PhoneNumber.fromJson(itemJson)?.let { list.add(it) }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveUpgrades(upgrades: Map<String, Int>) {
        val serialized = upgrades.entries.joinToString(";") { "${it.key}:${it.value}" }
        prefs.edit().putString("key_upgrades", serialized).apply()
    }

    fun loadUpgrades(): Map<String, Int> {
        val serialized = prefs.getString("key_upgrades", null) ?: return emptyMap()
        return try {
            serialized.split(";").mapNotNull {
                val pair = it.split(":")
                if (pair.size == 2) pair[0] to (pair[1].toIntOrNull() ?: 0) else null
            }.toMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveQuests(quests: List<Quest>) {
        val serialized = quests.joinToString("~") {
            "${it.id}#${it.currentProgress}#${it.isClaimed}"
        }
        prefs.edit().putString("key_quests", serialized).apply()
    }

    fun loadQuestsProgress(): Map<String, Pair<Int, Boolean>> {
        val serialized = prefs.getString("key_quests", null) ?: return emptyMap()
        return try {
            serialized.split("~").mapNotNull { entry ->
                val tokens = entry.split("#")
                if (tokens.size >= 3) {
                    val id = tokens[0]
                    val prog = tokens[1].toIntOrNull() ?: 0
                    val claimed = tokens[2].toBoolean()
                    id to Pair(prog, claimed)
                } else null
            }.toMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }
}

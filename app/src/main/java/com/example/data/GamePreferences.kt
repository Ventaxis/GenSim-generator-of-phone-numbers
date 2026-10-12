package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.PhoneNumber
import com.example.model.Quest
import com.example.model.RarityTier
import org.json.JSONArray

class GamePreferences(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sim_game_state", Context.MODE_PRIVATE)

    init {
        migrateFromLegacyPrefFiles()
    }

    private fun migrateFromLegacyPrefFiles() {
        val legacyNames = listOf(
            "${context.packageName}_preferences",
            "game_prefs",
            "sim_tycoon",
            "user_data",
            "preferences"
        )
        for (name in legacyNames) {
            try {
                val old = context.getSharedPreferences(name, Context.MODE_PRIVATE)
                val allEntries = old.all
                if (allEntries.isNotEmpty()) {
                    val editor = prefs.edit()
                    for ((k, v) in allEntries) {
                        if (!prefs.contains(k)) {
                            when (v) {
                                is String -> editor.putString(k, v)
                                is Boolean -> editor.putBoolean(k, v)
                                is Int -> editor.putInt(k, v)
                                is Long -> editor.putLong(k, v)
                                is Float -> editor.putFloat(k, v)
                                is Set<*> -> @Suppress("UNCHECKED_CAST") editor.putStringSet(k, v as Set<String>)
                            }
                        }
                    }
                    editor.apply()
                }
            } catch (_: Exception) {}
        }
    }

    private fun getSafeLong(keys: List<String>, defaultVal: Long): Long {
        for (k in keys) {
            if (prefs.contains(k)) {
                try {
                    return prefs.getLong(k, defaultVal)
                } catch (_: Exception) {
                    try {
                        return prefs.getInt(k, defaultVal.toInt()).toLong()
                    } catch (_: Exception) {
                        try {
                            val str = prefs.getString(k, null)
                            if (str != null) {
                                return str.toLongOrNull() ?: defaultVal
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
        return defaultVal
    }

    private fun getSafeString(keys: List<String>): String? {
        for (k in keys) {
            if (prefs.contains(k)) {
                try {
                    val s = prefs.getString(k, null)
                    if (!s.isNullOrBlank()) return s
                } catch (_: Exception) {}
            }
        }
        return null
    }

    fun saveBalance(balance: Long) {
        prefs.edit().putLong("key_balance", balance).apply()
    }

    fun loadBalance(defaultVal: Long = 2_500L): Long {
        return getSafeLong(listOf("key_balance", "balance", "user_balance", "money"), defaultVal)
    }

    fun savePlayerXp(xp: Long) {
        prefs.edit().putLong("key_player_xp", xp).apply()
    }

    fun loadPlayerXp(): Long {
        return getSafeLong(listOf("key_player_xp", "player_xp", "xp", "player_level"), 0L)
    }

    fun saveStats(
        rolls: Long,
        earned: Long,
        bestNumber: PhoneNumber? = null,
        rarityCounts: Map<RarityTier, Int>? = null
    ) {
        val editor = prefs.edit()
            .putLong("key_total_rolls", rolls)
            .putLong("key_total_earned", earned)

        if (bestNumber != null) {
            editor.putString("key_best_number_json", bestNumber.toJson())
        }

        if (rarityCounts != null) {
            val countsSerialized = rarityCounts.entries.joinToString(";") { "${it.key.name}:${it.value}" }
            editor.putString("key_rarity_counts", countsSerialized)
        }

        editor.apply()
    }

    data class SavedStatsData(
        val totalRolls: Long,
        val totalEarned: Long,
        val bestNumber: PhoneNumber?,
        val rarityCounts: Map<RarityTier, Int>
    )

    fun loadStats(): SavedStatsData {
        val rolls = getSafeLong(listOf("key_total_rolls", "total_rolls", "rolls", "rolls_count"), 0L)
        val earned = getSafeLong(listOf("key_total_earned", "total_earned", "earned", "total_money_earned"), 0L)
        val bestJson = getSafeString(listOf("key_best_number_json", "key_best_number", "best_number", "best_number_json"))
        val bestNumber = if (bestJson != null) PhoneNumber.fromJson(bestJson) else null

        val countsSerialized = getSafeString(listOf("key_rarity_counts", "rarity_counts", "rarities"))
        val counts = mutableMapOf<RarityTier, Int>()
        if (!countsSerialized.isNullOrBlank()) {
            try {
                countsSerialized.split(";").forEach { item ->
                    val pair = item.split(":")
                    if (pair.size == 2) {
                        try {
                            val tier = RarityTier.valueOf(pair[0].uppercase())
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
            prefs.edit().remove("key_active_sim_json").remove("key_active_sim").remove("active_sim").apply()
            return
        }
        prefs.edit().putString("key_active_sim_json", sim.toJson()).apply()
    }

    fun loadActiveSim(): PhoneNumber? {
        val raw = getSafeString(listOf(
            "key_active_sim_json",
            "key_active_sim",
            "active_sim",
            "current_active_sim",
            "active_number",
            "active_phone"
        ))
        if (raw != null) {
            val parsed = PhoneNumber.fromJson(raw)
            if (parsed != null) return parsed
        }
        return null
    }

    fun saveCurrentNumber(sim: PhoneNumber?) {
        if (sim == null) {
            prefs.edit().remove("key_current_sim_json").remove("key_current_sim").remove("current_sim").apply()
            return
        }
        prefs.edit().putString("key_current_sim_json", sim.toJson()).apply()
    }

    fun loadCurrentNumber(): PhoneNumber? {
        val raw = getSafeString(listOf(
            "key_current_sim_json",
            "key_current_sim",
            "key_current_number",
            "current_sim",
            "current_number",
            "last_number"
        ))
        if (raw != null) {
            return PhoneNumber.fromJson(raw)
        }
        return null
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
        val raw = getSafeString(listOf(
            "key_inventory_backup_json",
            "key_inventory_backup",
            "key_inventory_json",
            "key_inventory",
            "inventory",
            "key_saved_sims",
            "saved_sims",
            "key_saved_numbers",
            "saved_numbers",
            "numbers",
            "vault",
            "key_vault"
        )) ?: return emptyList()

        return try {
            val trimmed = raw.trim()
            val list = mutableListOf<PhoneNumber>()
            if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                for (i in 0 until arr.length()) {
                    val item = arr.get(i)
                    when (item) {
                        is String -> PhoneNumber.fromJson(item)?.let { list.add(it) }
                        is org.json.JSONObject -> PhoneNumber.fromJson(item.toString())?.let { list.add(it) }
                    }
                }
            } else if (trimmed.contains("\n") || trimmed.contains(";")) {
                val tokens = if (trimmed.contains("\n")) trimmed.split("\n") else trimmed.split(";")
                for (token in tokens) {
                    if (token.isNotBlank()) {
                        PhoneNumber.fromJson(token.trim())?.let { list.add(it) }
                    }
                }
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

    fun saveAutoSellConfig(config: com.example.model.AutoSellConfig) {
        prefs.edit()
            .putString("key_auto_sell_below_rarity", config.autoSellBelowRarity.name)
            .putLong("key_auto_sell_min_keep_price", config.minKeepPrice)
            .putBoolean("key_auto_sell_save_kept", config.autoSaveKeptToInventory)
            .putBoolean("key_auto_sell_stop_ultra", config.stopOnUltraRare)
            .apply()
    }

    fun loadAutoSellConfig(defaultConfig: com.example.model.AutoSellConfig): com.example.model.AutoSellConfig {
        val rarityStr = prefs.getString("key_auto_sell_below_rarity", null) ?: return defaultConfig
        val rarity = try {
            RarityTier.valueOf(rarityStr)
        } catch (_: Exception) {
            defaultConfig.autoSellBelowRarity
        }
        val minPrice = prefs.getLong("key_auto_sell_min_keep_price", defaultConfig.minKeepPrice)
        val saveKept = prefs.getBoolean("key_auto_sell_save_kept", defaultConfig.autoSaveKeptToInventory)
        val stopUltra = prefs.getBoolean("key_auto_sell_stop_ultra", defaultConfig.stopOnUltraRare)

        return com.example.model.AutoSellConfig(
            isAutoRolling = false,
            autoSellBelowRarity = rarity,
            minKeepPrice = minPrice,
            autoSaveKeptToInventory = saveKept,
            stopOnUltraRare = stopUltra
        )
    }
}

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_stats_log")
data class GameStatsEntity(
    @PrimaryKey val id: String = "stats_record",
    val totalRolls: Long = 0L,
    val totalEarned: Long = 0L,
    val bestNumberId: String? = null,
    val bestNumberFormatted: String? = null,
    val bestNumberValue: Long? = null,
    val bestNumberRarity: String? = null,
    val bestNumberJson: String? = null,
    val rarityCountsJson: String = "",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatsDao {

    @Query("SELECT * FROM game_stats_log WHERE id = 'stats_record'")
    fun getStatsFlow(): Flow<GameStatsEntity?>

    @Query("SELECT * FROM game_stats_log WHERE id = 'stats_record'")
    suspend fun getStats(): GameStatsEntity?

    @Query("SELECT * FROM game_stats_log")
    suspend fun getAllStatsRecords(): List<GameStatsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: GameStatsEntity)

    @Query("DELETE FROM game_stats_log")
    suspend fun clear()
}

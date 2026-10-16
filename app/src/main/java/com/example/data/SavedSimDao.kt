package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSimDao {

    @Query("SELECT * FROM saved_sims ORDER BY timestamp DESC")
    fun getAllSavedSims(): Flow<List<SavedSimEntity>>

    @Query("SELECT * FROM saved_sims ORDER BY timestamp DESC")
    suspend fun getAllSavedSimsOnce(): List<SavedSimEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSim(sim: SavedSimEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sims: List<SavedSimEntity>)

    @Query("UPDATE saved_sims SET isInVault = :isInVault WHERE id = :id")
    suspend fun updateVaultStatus(id: String, isInVault: Boolean)

    @Query("DELETE FROM saved_sims WHERE id = :id")
    suspend fun deleteSimById(id: String)

    @Query("DELETE FROM saved_sims WHERE id IN (:ids)")
    suspend fun deleteSimsByIds(ids: List<String>)

    @Query("DELETE FROM saved_sims")
    suspend fun clearAll()
}

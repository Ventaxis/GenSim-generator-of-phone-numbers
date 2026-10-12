package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SavedSimEntity::class, GameStatsEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedSimDao(): SavedSimDao
    abstract fun gameStatsDao(): GameStatsDao

    /**
     * Diagnostic helper to query raw SQLite tables directly from Room.
     */
    fun getRawTableContent(tableName: String): List<Map<String, Any?>> {
        val result = mutableListOf<Map<String, Any?>>()
        try {
            val db = openHelper.readableDatabase
            val cursor = db.query("SELECT * FROM $tableName")
            cursor.use { c ->
                val colNames = c.columnNames
                while (c.moveToNext()) {
                    val row = mutableMapOf<String, Any?>()
                    for (i in colNames.indices) {
                        row[colNames[i]] = when (c.getType(i)) {
                            android.database.Cursor.FIELD_TYPE_NULL -> null
                            android.database.Cursor.FIELD_TYPE_INTEGER -> c.getLong(i)
                            android.database.Cursor.FIELD_TYPE_FLOAT -> c.getDouble(i)
                            android.database.Cursor.FIELD_TYPE_STRING -> c.getString(i)
                            android.database.Cursor.FIELD_TYPE_BLOB -> "[BLOB]"
                            else -> c.getString(i)
                        }
                    }
                    result.add(row)
                }
            }
        } catch (_: Exception) {}
        return result
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private fun addColumnIfNotExists(db: SupportSQLiteDatabase, table: String, columnDef: String, columnName: String) {
            var exists = false
            try {
                val cursor = db.query("PRAGMA table_info(`$table`)")
                cursor.use { c ->
                    val nameIndex = c.getColumnIndex("name")
                    if (nameIndex != -1) {
                        while (c.moveToNext()) {
                            if (c.getString(nameIndex).equals(columnName, ignoreCase = true)) {
                                exists = true
                                break
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            if (!exists) {
                try {
                    db.execSQL("ALTER TABLE `$table` ADD COLUMN $columnDef")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "saved_sims", "isInVault INTEGER NOT NULL DEFAULT 0", "isInVault")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `game_stats_log` (" +
                    "`id` TEXT NOT NULL PRIMARY KEY, " +
                    "`totalRolls` INTEGER NOT NULL, " +
                    "`totalEarned` INTEGER NOT NULL, " +
                    "`bestNumberId` TEXT, " +
                    "`bestNumberFormatted` TEXT, " +
                    "`bestNumberValue` INTEGER, " +
                    "`bestNumberRarity` TEXT, " +
                    "`bestNumberJson` TEXT, " +
                    "`rarityCountsJson` TEXT NOT NULL, " +
                    "`lastUpdatedTimestamp` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "saved_sims", "isInVault INTEGER NOT NULL DEFAULT 0", "isInVault")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `game_stats_log` (" +
                    "`id` TEXT NOT NULL PRIMARY KEY, " +
                    "`totalRolls` INTEGER NOT NULL, " +
                    "`totalEarned` INTEGER NOT NULL, " +
                    "`bestNumberId` TEXT, " +
                    "`bestNumberFormatted` TEXT, " +
                    "`bestNumberValue` INTEGER, " +
                    "`bestNumberRarity` TEXT, " +
                    "`bestNumberJson` TEXT, " +
                    "`rarityCountsJson` TEXT NOT NULL, " +
                    "`lastUpdatedTimestamp` INTEGER NOT NULL)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sim_tycoon.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

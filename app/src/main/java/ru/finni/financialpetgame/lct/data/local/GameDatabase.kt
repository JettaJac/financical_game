package ru.finni.financialpetgame.lct.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [GoalRecordEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun goalHistoryDao(): GoalHistoryDao
}

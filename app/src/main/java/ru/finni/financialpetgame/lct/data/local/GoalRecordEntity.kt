package ru.finni.financialpetgame.lct.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "goal_records",
    indices = [
        Index(value = ["goal_code"]),
        Index(value = ["completed_at"]),
    ],
)
data class GoalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "goal_code")
    val goalCode: String,
    @ColumnInfo(name = "title_snapshot")
    val titleSnapshot: String,
    @ColumnInfo(name = "target_value")
    val targetValue: Long,
    @ColumnInfo(name = "current_value")
    val currentValue: Long = 0,
    @ColumnInfo(name = "started_at")
    val startedAt: Long,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
)

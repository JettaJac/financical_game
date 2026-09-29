package ru.finni.financialpetgame.lct.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalHistoryDao {
    @Insert
    suspend fun insert(goal: GoalRecordEntity): Long

    @Query(
        """
        SELECT * FROM goal_records
        WHERE completed_at IS NULL
        ORDER BY started_at DESC
        LIMIT 1
        """
    )
    fun observeActiveGoal(): Flow<GoalRecordEntity?>

    @Query(
        """
        SELECT * FROM goal_records
        WHERE completed_at IS NOT NULL
        ORDER BY completed_at DESC
        """
    )
    fun observeHistory(): Flow<List<GoalRecordEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM goal_records
        WHERE completed_at IS NOT NULL
        """
    )
    fun observeCompletedCount(): Flow<Int>

    @Query(
        """
        UPDATE goal_records
        SET current_value = :value
        WHERE id = :goalRecordId AND completed_at IS NULL
        """
    )
    suspend fun updateProgress(goalRecordId: Long, value: Long): Int

    @Query(
        """
        UPDATE goal_records
        SET completed_at = :completedAt
        WHERE id = :goalRecordId
          AND completed_at IS NULL
          AND current_value >= target_value
        """
    )
    suspend fun complete(goalRecordId: Long, completedAt: Long): Int
}

package ru.finni.financialpetgame.lct.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import ru.finni.financialpetgame.lct.data.local.GameDatabase
import ru.finni.financialpetgame.lct.data.local.GoalHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideGameDatabase(
        @ApplicationContext context: Context,
    ): GameDatabase =
        Room.databaseBuilder<GameDatabase>(
            context = context,
            name = "financial_game.db",
        )
            .setDriver(AndroidSQLiteDriver())
            .build()

    @Provides
    fun provideGoalHistoryDao(database: GameDatabase): GoalHistoryDao =
        database.goalHistoryDao()
}

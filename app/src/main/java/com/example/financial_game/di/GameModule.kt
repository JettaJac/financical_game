package com.example.financial_game.di

import com.example.financial_game.data.GameStore
import com.example.financial_game.domain.GameRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class GameModule {
    @Binds
    abstract fun repository(store: GameStore): GameRepository
}

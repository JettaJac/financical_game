package ru.finni.financialpetgame.lct.di

import ru.finni.financialpetgame.lct.data.GameStore
import ru.finni.financialpetgame.lct.domain.GameRepository
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

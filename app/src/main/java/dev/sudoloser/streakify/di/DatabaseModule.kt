package dev.sudoloser.streakify.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.sudoloser.streakify.data.local.StreakDatabase
import dev.sudoloser.streakify.data.local.dao.StreakDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StreakDatabase {
        return Room.databaseBuilder(
            context,
            StreakDatabase::class.java,
            "streak_database"
        ).build()
    }

    @Provides
    fun provideStreakDao(database: StreakDatabase): StreakDao {
        return database.streakDao()
    }
}

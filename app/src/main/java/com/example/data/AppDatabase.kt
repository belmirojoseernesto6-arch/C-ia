package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BattleVideo::class, ProducaoHibrida::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun battleVideoDao(): BattleVideoDao
    abstract fun producaoHibridaDao(): ProducaoHibridaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "guerreiros_battles_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

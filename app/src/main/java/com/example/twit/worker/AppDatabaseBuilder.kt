package com.example.twit.worker

import android.content.Context
import androidx.room.Room
import com.example.twit.data.AppDatabase

object AppDatabaseBuilder {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "scheduled_tweets_db"
            ).build().also { INSTANCE = it }
        }
    }
}

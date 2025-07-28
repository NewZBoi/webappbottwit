package com.example.twit.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledTweetDao {
    @Query("SELECT * FROM scheduled_tweets ORDER BY scheduledTime ASC")
    fun getAllTweets(): Flow<List<ScheduledTweetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tweet: ScheduledTweetEntity): Long

    @Delete
    suspend fun delete(tweet: ScheduledTweetEntity)

    @Query("UPDATE scheduled_tweets SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)
}

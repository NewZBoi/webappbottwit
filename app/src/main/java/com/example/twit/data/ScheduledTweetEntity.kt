package com.example.twit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_tweets")
data class ScheduledTweetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val publication: String,
    val url: String,
    val scheduledTime: Long,
    val status: String // "Pending", "Posted", "Failed"
)

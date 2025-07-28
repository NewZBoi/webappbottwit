package com.example.twit.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.twit.twitter.TwitterClient

class TweetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val tweetText = inputData.getString("tweet_text") ?: return Result.failure()
        val tweetUrl = inputData.getString("tweet_url") ?: ""

        return try {
            TwitterClient.postTweet("$tweetText $tweetUrl")
            Log.d("TweetWorker", "Tweet posted successfully: $tweetText")
            Result.success()
        } catch (e: Exception) {
            Log.e("TweetWorker", "Tweet failed", e)
            Result.retry()
        }
    }
}

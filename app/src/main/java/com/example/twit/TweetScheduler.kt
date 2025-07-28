package com.example.twit

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.twit.worker.TweetWorker
import java.util.concurrent.TimeUnit

object TweetScheduler {
    fun scheduleTweet(context: Context, preview: LinkPreviewData, postTime: Long) {
        val delay = postTime - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putString("tweet_text", "\"${preview.title}\" -${preview.siteName ?: "Unknown"}")
            .putString("tweet_url", preview.url)
            .build()

        val request = OneTimeWorkRequestBuilder<TweetWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}

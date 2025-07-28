package com.example.twit.twitter

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8

object TwitterClient {

    // 🔹 Replace with your actual keys
    private const val API_KEY = "SUtxHDiZjaawm1sKxqYs9cOgp"
    private const val API_SECRET_KEY = "cmPy2eoKuuGNnTCPlYfchDyk5oPKZDZMDzy7rXd4nob8d63IDc"
    private const val ACCESS_TOKEN = "1947645298097983488-562dGRKTW4KSbarLLZmmflIwLcmxAC"
    private const val ACCESS_TOKEN_SECRET = "dIYz6lOlSadTYNBzP24JChhez0ECM6ZxQWc9mybKRZC6p"

    private const val TWEET_URL = "https://api.twitter.com/1.1/statuses/update.json"

    suspend fun postTweet(status: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val nonce = UUID.randomUUID().toString().replace("-", "")
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            val params = TreeMap<String, String>().apply {
                put("oauth_consumer_key", API_KEY)
                put("oauth_nonce", nonce)
                put("oauth_signature_method", "HMAC-SHA1")
                put("oauth_timestamp", timestamp)
                put("oauth_token", ACCESS_TOKEN)
                put("oauth_version", "1.0")
                put("status", status)
            }

            val baseString = "POST&${URLEncoder.encode(TWEET_URL, UTF_8.name())}&" +
                    URLEncoder.encode(params.map { "${it.key}=${it.value}" }.joinToString("&"), UTF_8.name())

            val signingKey = "${URLEncoder.encode(API_SECRET_KEY, UTF_8.name())}&${URLEncoder.encode(ACCESS_TOKEN_SECRET, UTF_8.name())}"
            val signature = sign(baseString, signingKey)
            val authHeader = "OAuth " + params.filterKeys { it != "status" }
                .map { "${it.key}=\"${URLEncoder.encode(it.value, UTF_8.name())}\"" }
                .plus("oauth_signature=\"${URLEncoder.encode(signature, UTF_8.name())}\"")
                .joinToString(", ")

            val body = FormBody.Builder().add("status", status).build()
            val request = Request.Builder()
                .url(TWEET_URL)
                .post(body)
                .addHeader("Authorization", authHeader)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            val success = response.isSuccessful

            Log.d("TwitterClient", "Response: ${response.code} ${response.message}")
            response.close()
            return@withContext success

        } catch (e: Exception) {
            Log.e("TwitterClient", "Tweet failed", e)
            return@withContext false
        }
    }

    private fun sign(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key.toByteArray(UTF_8), "HmacSHA1"))
        return android.util.Base64.encodeToString(mac.doFinal(data.toByteArray(UTF_8)), android.util.Base64.NO_WRAP)
    }
}

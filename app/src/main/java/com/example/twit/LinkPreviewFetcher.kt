package com.example.twit

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.jsoup.Jsoup

object LinkPreviewFetcher {
    private val client = HttpClient(CIO)

    suspend fun fetchMetadata(url: String): LinkPreviewData {
        return try {
            val response: HttpResponse = client.get(url) {
                headers.append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                headers.append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                headers.append("Accept-Language", "en-US,en;q=0.9")
                headers.append("Connection", "keep-alive")
            }
            val html = response.bodyAsText()
            val doc = Jsoup.parse(html)

            val title = doc.select("meta[property=og:title]").attr("content")
                .ifEmpty { doc.title() }

            val description = doc.select("meta[property=og:description]").attr("content")
                .ifEmpty { doc.select("meta[name=description]").attr("content") }

            val image = doc.select("meta[property=og:image]").attr("content")

            LinkPreviewData(url, title, description, image)
        } catch (e: Exception) {
            LinkPreviewData(url, "Error loading preview", e.localizedMessage, null)
        }
    }
}

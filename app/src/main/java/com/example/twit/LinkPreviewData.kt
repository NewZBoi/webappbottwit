package com.example.twit

data class LinkPreviewData(
    val url: String,
    val title: String?,
    val imageUrl: String?,
    val siteName: String? = null   // ✅ Added siteName property
)

package com.cataloghub.android.model

import java.util.Date

/**
 * Data class representing a YouTube video.
 */
data class YouTubeVideo(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val publishedAt: Date,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val duration: String,
    val channelTitle: String
) 
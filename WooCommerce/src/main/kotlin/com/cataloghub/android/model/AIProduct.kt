package com.cataloghub.android.model

import java.util.Date

/**
 * Data class representing a product generated from a video.
 */
data class AIProduct(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val videoId: String,
    val videoTitle: String,
    val videoThumbnailUrl: String,
    val createdAt: Date,
    val status: AIProductStatus,
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

/**
 * Enum representing the status of a product.
 */
enum class AIProductStatus {
    PENDING,
    APPROVED,
    REJECTED
} 
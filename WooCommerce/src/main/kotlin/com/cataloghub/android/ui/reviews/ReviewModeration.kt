package com.cataloghub.android.ui.reviews

import com.cataloghub.android.model.ActionStatus
import com.cataloghub.android.model.ActionStatus.PENDING
import com.cataloghub.android.model.ProductReview

data class ReviewModerationRequest(
    val review: ProductReview,
    val newStatus: ProductReviewStatus,
) : Comparable<ReviewModerationRequest> {
    private val timeOfRequest = System.currentTimeMillis()

    override fun compareTo(other: ReviewModerationRequest): Int {
        return timeOfRequest.compareTo(other.timeOfRequest)
    }
}

data class ReviewModerationStatus(
    val request: ReviewModerationRequest,
    val actionStatus: ActionStatus = PENDING
) : Comparable<ReviewModerationStatus> {
    val review
        get() = request.review
    val newStatus
        get() = request.newStatus

    override fun compareTo(other: ReviewModerationStatus): Int {
        return request.compareTo(other.request)
    }
}

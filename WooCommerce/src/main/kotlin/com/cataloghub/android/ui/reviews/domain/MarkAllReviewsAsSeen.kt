package com.cataloghub.android.ui.reviews.domain

import com.cataloghub.android.model.RequestResult.*
import com.cataloghub.android.notifications.NotificationChannelType
import com.cataloghub.android.notifications.push.NotificationMessageHandler
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.reviews.ReviewListRepository
import javax.inject.Inject

class MarkAllReviewsAsSeen @Inject constructor(
    private val selectedSite: SelectedSite,
    private val repository: ReviewListRepository,
    private val notificationHandler: NotificationMessageHandler,
) {
    suspend operator fun invoke(): MarkReviewAsSeenResult =
        when (repository.markAllProductReviewsAsRead()) {
            ERROR,
            API_ERROR,
            RETRY -> Fail
            NO_ACTION_NEEDED,
            SUCCESS -> {
                notificationHandler.removeNotificationsOfTypeFromSystemsBar(
                    NotificationChannelType.REVIEW,
                    selectedSite.get().siteId
                )
                Success
            }
        }

    sealed class MarkReviewAsSeenResult
    object Fail : MarkReviewAsSeenResult()
    object Success : MarkReviewAsSeenResult()
}

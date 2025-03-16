package com.cataloghub.android.notifications

import androidx.annotation.StringRes
import com.cataloghub.android.notifications.NotificationChannelType.NEW_ORDER
import com.cataloghub.android.notifications.NotificationChannelType.OTHER
import com.cataloghub.android.notifications.NotificationChannelType.REVIEW

enum class NotificationChannelType {
    NEW_ORDER,
    REVIEW,
    OTHER
}

private const val GROUP_NOTIFICATION_ID_ORDER = 30001
private const val GROUP_NOTIFICATION_ID_REVIEW = 30002
private const val GROUP_NOTIFICATION_ID_OTHER = 30003

@StringRes
fun NotificationChannelType.getGroupId(): Int {
    return when (this) {
        NEW_ORDER -> GROUP_NOTIFICATION_ID_ORDER
        REVIEW -> GROUP_NOTIFICATION_ID_REVIEW
        OTHER -> GROUP_NOTIFICATION_ID_OTHER
    }
}

fun NotificationChannelType.shouldCircularizeNoteIcon(): Boolean {
    return when (this) {
        REVIEW -> true
        else -> false
    }
}

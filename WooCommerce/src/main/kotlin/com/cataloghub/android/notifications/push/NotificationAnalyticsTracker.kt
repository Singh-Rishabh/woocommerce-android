package com.cataloghub.android.notifications.push

import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.model.Notification
import com.cataloghub.android.tools.SelectedSite
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAnalyticsTracker @Inject constructor(
    private val selectedSite: SelectedSite,
    private val appPrefsWrapper: AppPrefsWrapper,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper
) {
    fun trackNotificationAnalytics(stat: AnalyticsEvent, notification: Notification) {
        val isFromSelectedSite = selectedSite.getIfExists()?.siteId == notification.remoteSiteId
        val properties = mutableMapOf<String, Any>()
        properties["notification_note_id"] = notification.remoteNoteId
        properties["notification_type"] = notification.noteType.trackingValue
        properties["push_notification_token"] = appPrefsWrapper.getFCMToken()
        properties["is_from_selected_site"] = isFromSelectedSite == true
        analyticsTrackerWrapper.track(stat, properties)
    }

    fun flush() {
        analyticsTrackerWrapper.flush()
    }
}
